#!/usr/bin/env bash

openssl genpkey -algorithm RSA -out private.pem
openssl pkcs8 -topk8 -inform PEM -outform PEM -in private.pem -out private_pkcs8.pem -nocrypt
openssl rsa -pubout -in private_pkcs8.pem -out public.pem
mv private.pem app.key
mv public.pem app.pub