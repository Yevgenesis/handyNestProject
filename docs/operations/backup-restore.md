# Резервное копирование и восстановление HandyNest

Целевые показатели: `RPO 24h`, `RTO 8h`.

## PostgreSQL

- ежедневно создавать зашифрованную копию custom format;
- хранить минимум одну копию вне application host;
- локальная копия: `make db-backup`;
- сначала проверять восстановление в изолированной среде;
- локальное destructive-восстановление: `make db-restore BACKUP_FILE=/absolute/path/file.dump CONFIRM=YES`;
- после восстановления запускать Liquibase validation, `mvn -B clean verify` и marketplace smoke.

Backup содержит персональные данные. Требуются encryption at rest/in transit, ограниченный IAM и журнал доступа. Каталог `backups/` нельзя коммитить.

## MinIO / S3

- bucket versioning и lifecycle policy не заменяют backup;
- ежедневно зеркалировать `handynest-public`, `handynest-private`, `handynest-verification` в отдельный account или region;
- для зеркалирования использовать `mc mirror --overwrite --remove` либо native replication провайдера;
- private и verification buckets шифровать отдельными managed keys;
- PostgreSQL metadata и object backup должны попадать в одно recovery window.

## Ежемесячная проверка восстановления

1. Развернуть чистые PostgreSQL и object storage.
2. Восстановить последнюю копию без доступа production-приложения.
3. Проверить Liquibase checksums, Hibernate `validate`, количество записей критических таблиц и доступность выборочных объектов.
4. Запустить auth и marketplace smoke.
5. Зафиксировать фактические RPO/RTO, ошибки и корректирующие действия.
