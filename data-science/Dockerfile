FROM python:3.13-slim

WORKDIR /app

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

RUN apt-get update \
    && apt-get install -y --no-install-recommends libgomp1 \
    && rm -rf /var/lib/apt/lists/*

COPY requirements-ml.txt .
COPY requirements-inference.txt .

RUN pip install --no-cache-dir -r requirements-inference.txt

COPY inference_api ./inference_api
COPY artifacts ./artifacts
COPY data/metadata ./data/metadata

EXPOSE 8000

CMD ["sh", "-c", "python -m uvicorn inference_api.app.main:app --host 0.0.0.0 --port ${PORT:-8000}"]
