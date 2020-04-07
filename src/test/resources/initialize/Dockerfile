FROM fedora:25

LABEL maintainer="support@xebialabs.com"

COPY . /tmp

CMD sleep 10s && /tmp/wait-for-it.sh $XLR_HOST -t 120 && /tmp/run.sh
