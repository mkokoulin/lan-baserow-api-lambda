---
name: project-baserow-sdk
description: Baserow submodule рефакторинг в Maven SDK — пакет, зависимости, новые классы
metadata:
  type: project
---

Модуль `baserow/` переработан в полноценный Maven SDK.

**Why:** Пользователь хотел библиотеку, пригодную для публикации в Maven и подключения в сторонние проекты.

**Что сделано:**
- Пакет переименован: `com.lan.app.infrastructure.baserow` → `com.lan.baserow`
- `build.gradle.kts`: плагины `java-library` + `maven-publish`, версия `1.0.0`, задачи `sourcesJar` + `javadocJar`
- Зависимости: Jakarta EE / MicroProfile как `compileOnly`, `jackson-annotations` и `slf4j-api` как `api`
- JBoss logging заменён на SLF4J в `AbstractBaserowRepository` и `BaserowErrorLoggingFilter`
- `@ApplicationScoped` удалён из `BaserowErrorLoggingFilter` (стал plain-class); в `BaserowAuthHeaders` оставлен (нужен для `@RegisterProvider`)
- Новые классы: `BaserowFilterOperator` (enum 35 операторов), `BaserowFilterType`, `BaserowFilter` (record + фабрики), `BaserowRowQuery` (fluent builder)
- Артефакты: `baserow-1.0.0.jar`, `baserow-1.0.0-sources.jar`, `baserow-1.0.0-javadoc.jar`
- 59 тестов, 0 ошибок
- Основное приложение в `src/` обновлено: добавлены явные импорты из нового пакета

**How to apply:** При дальнейших изменениях библиотеки использовать пакет `com.lan.baserow.*`. Для публикации в Maven Central потребуется добавить signing и раскомментировать GitHub Packages репозиторий в `baserow/build.gradle.kts`.
