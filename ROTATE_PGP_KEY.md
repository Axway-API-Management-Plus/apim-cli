# Rotate PGP Keys.

PGP key comes with expiry date. 


## Create new key

```bash
➜  apim-cli git:(master) gpg --gen-key
gpg (GnuPG) 2.4.5; Copyright (C) 2024 g10 Code GmbH
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law.

Note: Use "gpg --full-generate-key" for a full featured key generation dialog.

GnuPG needs to construct a user ID to identify your key.

Real name: Rathna
Email address: rathnapandi.n@gmail.com
You selected this USER-ID:
    "Rathna <rathnapandi.n@gmail.com>"

Change (N)ame, (E)mail, or (O)kay/(Q)uit? O
```

## Push the key to Key server

Use the pgp id to upload it key server

```bash
 gpg --keyserver keyserver.ubuntu.com --send-keys 5D8F776E941F2D1D91EB2875212961A21019826F
```

## Store the key password and private key to Github action secrets and variables. 

- Secret names
  - GPG_PASSPHRASE
  - GPG_PRIVATE_KEY

### Export private key
```bash
gpg --output private.pgp --armor --export-secret-key 5D8F776E941F2D1D91EB2875212961A21019826F
```
