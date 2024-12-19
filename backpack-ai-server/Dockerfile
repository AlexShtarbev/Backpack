FROM openjdk:23-jdk
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

ARG phantomjs_version="2.1.1"

ENV PATH="/home/abc/.venv/bin:$PATH" \
    PUID="911" \
    PGID="911" \
    UMASK="022"

RUN set -x && \
    groupadd --gid "$PGID" abc && \
    useradd \
        -m \
        -s /bin/bash \
        -u $PUID \
        -g abc \
        -G abc \
        abc

RUN set -x && \
    microdnf update -y && \
    microdnf install -y \
        epel-release && \
    microdnf install -y wget && \
    wget https://download.docker.com/linux/centos/docker-ce.repo -O /etc/yum.repos.d/docker-ce.repo && \
    microdnf install -y \
        supervisor \
        file \
        wget \
        python3 \
        python3-devel \
        xz \
        python3-pip && \
    microdnf clean all && \
    python3 -m venv /home/abc/.venv

# Download and install FFmpeg
RUN wget https://johnvansickle.com/ffmpeg/releases/ffmpeg-release-amd64-static.tar.xz
RUN tar xf ffmpeg-release-amd64-static.tar.xz -C /tmp/
RUN chmod -R a+x /tmp/ffmpeg-*/*
RUN ls -la /tmp/ffmpeg-*/
RUN mv /tmp/ffmpeg-*/ffmpeg  /usr/local/bin/
RUN mv /tmp/ffmpeg-*/ffprobe /usr/local/bin/
RUN rm -rf /tmp/*

# Set permissions for FFmpeg
RUN chmod +x /usr/local/bin/ffmpeg

RUN set -x && /home/abc/.venv/bin/pip --no-cache-dir install yt-dlp[default]

COPY start.sh .
RUN chmod 0755 start.sh
RUN chmod +x start.sh

ENTRYPOINT ["/start.sh"]
