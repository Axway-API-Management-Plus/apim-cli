#!/bin/sh

# Create an API-Development Organisation!
curl --insecure -u apiadmin:changeme -X POST \
  https://localhost:8075/api/portal/v1.3/organizations \
  -H 'Content-Type: application/json' \
  -d '{
  "name": "API Development",
  "description": "Test Org",
  "enabled": true,
  "development": true
}'
echo "Created organization: 'API Development'"

