# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & deploy

- Build the WAR: `mvn package` → produces `target/nettools.war` (`<finalName>` in `pom.xml:59`). Maven is configured for Java 8 source/target; there is no surefire/test configuration and no tests in the tree.
- Build the container: `docker build --rm -f Dockerfile -t evandromoura/bigstalker:latest .` (see `kubernetes/bin/buildDocker.sh`). The Dockerfile expects `target/nettools.war` to already exist — run `mvn package` first. Base image is `evandromoura/wildfly:24.0.2.Final.itm` (WildFly 24, Java EE 8 / `javax.*` — not Jakarta).
- Runtime stack baked into the image: custom `kubernetes/standalone.xml` (full-HA profile with KUBE_PING JGroups), a bundled PostgreSQL JDBC driver jar, and a `docker-entrypoint.sh` that launches `standalone.sh` binding to `$MY_POD_IP` for Kubernetes pod networking.
- The app requires external CLI binaries on the container: `ping`, `nmap`, `nslookup`, `whois`, `traceroute`, `snmpwalk`, `dirb`, `youtube-dl`, `googler`. Services shell out via `Runtime.exec` — they will silently return empty output if a binary is missing from the host.

## Architecture

This is a JSF 2.2 + CDI + JAX-RS web app where each "net tool" (ping, traceroute, whois, nmap, snmp, sip, nslookup, dirb/pentest, youtube download, peoplesearch) is implemented by a parallel set of classes following an identical pattern. When adding or modifying a tool, expect to touch every layer:

- `controller/<tool>/<Tool>Controller.java` — JSF backing bean (`@Model` or `@Named @ViewScoped`), extends `AbstractController<TO>`. Holds form state, seeds defaults in `@PostConstruct comporInformacoesHTTP()`, appends a new TO to the session's history list on submit.
- `service/<tool>/<Tool>Service.java` — `@Named` CDI bean with an `executar(TO, Session)` method. Builds a shell command via `String.format(...)`, runs `Runtime.getRuntime().exec(command)`, streams each stdout/stderr line to the optional WebSocket `Session` as JSON (via Gson), and returns the aggregated output as a String. Always sends `"FIM"` as a sentinel when streaming completes.
- `websocket/<Tool>Server.java` — `@ServerEndpoint("/<Tool>Server")`, receives a JSON-serialized TO from the client, deserializes with Gson, injects the matching service, calls `executar(to, session)` to stream results back. This is how the UI gets live command output.
- `api/ToolsApiServer.java` — single JAX-RS resource at `/api/tools/*` (`JAXRSConfiguration` sets `@ApplicationPath("api")`). Exposes each tool as a `@GET` with `@QueryParam`s, calls the same service with a `null` Session (so no streaming — just the aggregated String result).
- `to/<Tool>TO.java` — plain data classes shared across controller/service/websocket/REST layers.
- `session/CustomIdentity.java` — `@SessionScoped @Named` bean holding per-user history lists (`pings`, `whoiss`, `nmaps`, …). Controllers append to these on each submit; views render the history panels from them.
- `view/NettoolsView.java` — PrettyFaces `@URLMappings` that map clean URLs (`/ping`, `/whois`, `/blog/article-1`, …) to `pages/<tool>/<tool>.xhtml` Facelets under `src/main/webapp/pages/`.

Cross-cutting:
- `AbstractController` provides FacesContext accessors, i18n lookup (`getMessage` via `ResourceBundle.getBundle("resources", ...)`), FacesMessage helpers (keys `sucesso` / `erro`), a download helper, generic `getTo()` that instantiates the TO via reflection on the parameterized superclass, and a multi-header `getClientIpAddr()` that walks every common proxy header.
- i18n bundles are `src/main/resources/resources_en.properties` and `resources_pt_BR.properties`; default locale is English (`faces-config.xml`). Code, comments, and FacesMessages are written in Portuguese (e.g. `executar`, `adicionarMensagem`, `comporInformacoesHTTP`) even though the UI is bilingual.
- `persistence.xml` declares a JTA unit with `schema-generation.database.action=drop-and-create` but no `@Entity` classes exist — it is unused. The app is stateless and keeps all data in session scope; do not assume there is a database layer. `standalone.xml` defines a `NettoolsDS` pointing at PostgreSQL anyway, which is a leftover.
- `servlet/DownloadYoutube.java` and `servlet/FacebookLogin.java` are simple file-streaming servlets (`/download/youtube`, `/facebook/login`) that read `?file=` and stream the file back — used by the youtube service to serve the downloaded media produced in its working directory.

When extending a tool, keep the `TO` symmetrical across layers (controller seeds it, service consumes it, websocket deserializes it from the same JSON shape, JAX-RS builds it from query params), and add the session history list + getter in `CustomIdentity` if the tool should retain per-user history.

## Conventions to preserve

- Logging is plain `System.out.println` / `System.err.println`. There is no logging framework wired up — follow the existing style rather than introducing SLF4J/JBoss Logging for one-off changes.
- Services catch `Exception` broadly and swallow it (often with the `e.printStackTrace()` commented out). This is deliberate for the "run a shell command and stream whatever comes back" flow — don't "fix" it by rethrowing, since the WebSocket is the user-visible error channel.
- Service commands are built with `String.format` against user-supplied `TO` fields and run through `Runtime.exec(String)`. Any change to input plumbing (controller, websocket, REST) flows straight into a shell command, so validate at the entry point or switch to the `exec(String[])` form rather than patching downstream.
- Ignore/delete `hs_err_pid*.log` at the repo root — those are JVM crash dumps, not source.
