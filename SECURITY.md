# Security Policy

## Supported versions

| Version | Supported |
|---------|-----------|
| 1.0.x | Yes |

## Reporting a vulnerability

Please report vulnerabilities privately. Do not open a public issue.

- Email: nicolasdwidharma@gmail.com
- Include: what you found, how to reproduce it, and the impact you expect
- You will get an acknowledgement within 7 days

## Scope notes

Kalkulasa is a fully offline app. It has no backend, no account system and
makes no network connections. Data lives in an SQLCipher-encrypted database on
the device. Backups the user exports are plain JSON files and are the
responsibility of the device the user stores them on.
