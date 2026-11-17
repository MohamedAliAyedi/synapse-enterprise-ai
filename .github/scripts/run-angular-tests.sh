#!/usr/bin/env bash
set -euo pipefail

echo "Running Angular unit tests..."
cd frontend
npm ci
npm run ci:test
