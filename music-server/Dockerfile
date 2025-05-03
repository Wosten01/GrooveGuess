FROM python:alpine

WORKDIR /app

COPY server.py /app/

RUN mkdir -p /app/music

EXPOSE 8000

CMD ["python", "server.py"]