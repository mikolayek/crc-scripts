# crc-scripts
Cloud Readiness Check - Scripts repository


# Usage

`crc-scripts-executor.groovy` should be run in hac on CCv2 environment.

Script runs scripts available in `scripts` folder in the sequence listed in `scripts/list.txt` file.

## Audited Types

Checks System configuration in terms of auditing, which is related to GDPR.
In the results it is possible to find global audit property `auditingEnabled`, if that is enabled or not.
Array property `auditedItems` lists all types which are configured for auditing.
