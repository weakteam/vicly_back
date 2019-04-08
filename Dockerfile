FROM opensuse/leap
MAINTAINER BlessedVictim
RUN  zypper -n install java-1_8_0-openjdk
ADD ./target/universal/backend-0.1.tgz /backend/
ENV SECRET_KEY="12345"
ENV MESSAGE_KEY="4567"
CMD ./backend/backend-0.1/bin/backend -Dhttp.port=8777
