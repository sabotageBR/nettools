# Use latest jboss/base-jdk:11 image as the base
FROM jboss/base-jdk:11

# Set the WILDFLY_VERSION env variable
ENV WILDFLY_VERSION 24.0.0.Final
ENV WILDFLY_SHA1 553fd92598a6af797451a9274f82de20b03fb508
ENV JBOSS_HOME /opt/jboss/wildfly

USER root

RUN yum install net-snmp net-snmp-utils nmap traceroute net-tools whois bind-utils epel-release pip -y
RUN yum install http://www6.atomicorp.com/channels/atomic/centos/7/x86_64/RPMS/dirb-222-6934.el7.art.x86_64.rpm -y
RUN yum install youtube-dl -y
RUN yum localinstall --nogpgcheck https://download1.rpmfusion.org/free/el/rpmfusion-free-release-7.noarch.rpm -y
RUN yum install ffmpeg ffmpeg-devel -y
RUN curl -o /usr/local/bin/googler https://raw.githubusercontent.com/jarun/googler/v4.3.2/googler && chmod +x /usr/local/bin/googler
RUN echo 'export PYTHONIOENCODING=utf8' >> /etc/profile
RUN source /etc/profile
COPY kubernetes/common.txt /usr/share/dirb/wordlists/common.txt

ENV TZ=America/Sao_Paulo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN echo '192.168.12.50   nettools.live' >> /etc/hosts

# Add the WildFly distribution to /opt, and make wildfly the owner of the extracted tar content
# Make sure the distribution is available from a well-known place
RUN cd $HOME \
    && curl -O https://download.jboss.org/wildfly/$WILDFLY_VERSION/wildfly-$WILDFLY_VERSION.tar.gz \
    && sha1sum wildfly-$WILDFLY_VERSION.tar.gz | grep $WILDFLY_SHA1 \
    && tar xf wildfly-$WILDFLY_VERSION.tar.gz \
    && mv $HOME/wildfly-$WILDFLY_VERSION $JBOSS_HOME \
    && rm wildfly-$WILDFLY_VERSION.tar.gz \
    && chown -R jboss:0 ${JBOSS_HOME} \
    && chmod -R g+rw ${JBOSS_HOME}

# Ensure signals are forwarded to the JVM process correctly for graceful shutdown
ENV LAUNCH_JBOSS_IN_BACKGROUND true

USER jboss

# Expose the ports we're interested in
EXPOSE 8080

# Set the default command to run on boot
# This will boot WildFly in the standalone mode and bind to all interface
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]