FROM node

RUN git clone https://github.com/muaz-khan/RTCMultiConnection-Server \
    && cd RTCMultiConnection-Server \
    && npm install -g npm@7.18.1 \
	&& npm install

CMD (cd RTCMultiConnection-Server && node server.js --ssl)

RUN echo '192.168.12.50   webrtc.nettools.live' >> /etc/hosts

EXPOSE 9001