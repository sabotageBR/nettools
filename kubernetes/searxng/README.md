# SearXNG no Kubernetes

Instancia interna de [SearXNG](https://github.com/searxng/searxng) que alimenta a
busca de pessoas (`/peoplesearch`) do nettools.

Sem ela, `live.nettools.util.SearxngClient` cai no default `http://localhost:8888`,
nao encontra nada escutando dentro do pod do WildFly, e o
`PeoplesearchService` devolve ao browser
`"Search temporarily unavailable (SearXNG unreachable at ...)"`.

Exposicao **apenas ClusterIP** — nao ha Ingress nem NodePort. O unico consumidor
e o pod do nettools.

## Contrato com a aplicacao

`SearxngClient.java:61-70` faz exatamente isto:

```
GET http://searxng:8080/search?q=<termo>&format=json
Accept: application/json
User-Agent: nettools/1.0
connect/read timeout: 15s
```

E consome so tres campos de cada item de `results[]`: `title`, `url` e `content`
(`SearxngClient.java:84-113`).

## Deploy

```bash
# 1. Secret com a chave de sessao (NAO use o placeholder do 01-secret.yaml)
kubectl -n nettools create secret generic searxng-secret \
  --from-literal=SEARXNG_SECRET="$(openssl rand -hex 32)" \
  --dry-run=client -o yaml | kubectl apply -f -

# 2. Config + Valkey + SearXNG
kubectl apply -f 02-configmap.yaml -f 03-valkey.yaml -f 04-searxng.yaml

# 3. Aguardar
kubectl -n nettools rollout status deploy/valkey
kubectl -n nettools rollout status deploy/searxng
```

O `05-networkpolicy.yaml` e opcional e **exige ajuste** dos labels antes de aplicar
— leia o cabecalho do arquivo.

## Cabear o nettools

Os manifestos do proprio nettools nao vivem neste repositorio. O Deployment em
producao chama-se `nettools-live` (labels `app=nettools-live-server`,
`tier=Production`, imagem `evandromoura/nettools:live-<versao>`):

```bash
kubectl -n nettools set env deployment/nettools-live \
  SEARXNG_URL=http://searxng:8080
kubectl -n nettools rollout status deployment/nettools-live
```

`SearxngClient.baseUrl()` usa `System.getenv` (`SearxngClient.java:53`), que e
imutavel no processo — a variavel so vale apos restart do pod, que o `set env` ja
provoca.

> **Este `set env` e imperativo.** Persista `SEARXNG_URL` no manifesto que voce
> versiona fora daqui, senao o proximo `apply` reverte e o peoplesearch volta a
> quebrar.

## Verificacao

Reproduzindo o metodo e os headers exatos do client Java:

```bash
kubectl -n nettools run curltest --rm -it --restart=Never --image=curlimages/curl -- \
  curl -s -o /dev/null -w '%{http_code}\n' \
  -H 'User-Agent: nettools/1.0' -H 'Accept: application/json' \
  'http://searxng:8080/search?q=teste&format=json'
```

Tem que responder **200**.

| Codigo | Causa provavel |
|---|---|
| `403` | `search.formats` sem `json` no `settings.yml` (`searx/webapp.py:630`) |
| `429` | limiter ligado — o metodo `http_accept` rejeita `Accept` sem `text/html` |
| timeout / conn refused | Service ou pod fora do ar; confira `kubectl -n nettools get pods` |

Depois confira o corpo e que os tres campos usados vem preenchidos:

```bash
kubectl -n nettools run curltest --rm -it --restart=Never --image=curlimages/curl -- \
  curl -s -H 'User-Agent: nettools/1.0' -H 'Accept: application/json' \
  'http://searxng:8080/search?q=teste&format=json' \
  | head -c 800
```

Vale testar tambem a query real do servico, que usa o operador `site:`
(`PeoplesearchService.java:112`), porque alguns engines degradam com ele:
`q=fulano site:instagram.com`.

Por fim, ponta a ponta: abrir `/peoplesearch`, buscar um nome, e confirmar que
`kubectl -n nettools logs deploy/<nettools> | grep -i searxng` **nao** mostra
`"Falha ao consultar SearXNG"`.

A busca e lenta por desenho: `THROTTLE_MILLIS = 1000` entre queries
(`PeoplesearchService.java:36`) e uma unica worker-thread global no
`PeoplesearchServer`. Nome + 4 redes leva ~9s. Isso e esperado.

## Por que o limiter esta desligado

O metodo `http_accept` da botdetection responde **429 para qualquer request cujo
header `Accept` nao contenha `text/html`**, e o `SearxngClient` manda
`Accept: application/json`. Com `server.limiter: true` o peoplesearch quebra —
e quebra em silencio, porque `PeoplesearchService.java:116-120` engole a excecao.

Como o Service e ClusterIP-only, nao ha superficie de abuso externa, entao
desligar o limiter nao abre risco real.

**Consequencia:** o Valkey fica ocioso. Ele e o unico componente que o limiter
usa. Esta deployado e cabeado (`SEARXNG_VALKEY_URL`) so para que ligar o limiter
depois seja trivial. Se preferir economizar recursos, remova `03-valkey.yaml` —
nada mais depende dele.

### Se quiser ligar o limiter mesmo assim

O `limiter.toml` ja vai pronto com `pass_ip`. O upstream documenta que
`ip_lists` tem prioridade sobre todos os outros metodos: um IP em `pass_ip` tem
acesso irrestrito e nem chega a ser checado por user-agent/accept.

1. O `pass_ip` ja esta com `10.32.0.0/12`, o range do weave-net deste cluster.
   Se trocar de CNI, reconfira o IP real dos pods:
   ```bash
   kubectl -n nettools get pods -o wide
   ```
2. Ajuste `pass_ip` no `02-configmap.yaml` se necessario.
3. Troque `limiter: false` para `true` no `settings.yml`.
4. `kubectl apply -f 02-configmap.yaml && kubectl -n nettools rollout restart deploy/searxng`
5. Rode a verificacao de novo. Se voltar `429`, o `pass_ip` nao esta batendo.

## Notas de manutencao

- Mudou o `02-configmap.yaml`? O pod nao recarrega sozinho:
  `kubectl -n nettools rollout restart deploy/searxng`.
- A tag da imagem e fixada de proposito. Para atualizar, escolha uma tag nova em
  https://github.com/searxng/searxng/pkgs/container/searxng e edite
  `04-searxng.yaml` — nao troque por `:latest`.
- `search.formats` **nao tem** variavel de ambiente equivalente; so pode ser
  definido via `settings.yml`. Ja `secret_key`, `limiter`, `public_instance`,
  `image_proxy`, `method`, `base_url` e `valkey.url` aceitam override por
  `SEARXNG_*` (ver `searx/settings_defaults.py`).
- Nao adicione `runAsNonRoot` nem `fsGroup` ao Deployment do SearXNG: a imagem
  inicia como root de proposito, faz `chown` dos volumes e so entao larga
  privilegio para o usuario `searxng`.
