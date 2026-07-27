# Security Policy

This document describes how to report vulnerabilities in `spring-data-requery`, the
currently supported versions, secure-configuration requirements for consumers, and the
remediation roadmap for known issues.

## Reporting a Vulnerability

Please report suspected vulnerabilities **privately** — do not open a public GitHub issue
for security reports.

- Use **GitHub Security Advisories** ("Report a vulnerability" on the repository's *Security* tab), or
- Email the maintainers / Coupang security team with the details below.

Include: affected version(s), a description and impact, reproduction steps or a proof of
concept, and any suggested remediation. We aim to acknowledge reports within **5 business
days** and to agree a disclosure timeline with the reporter. Please allow a reasonable
period for a fix before any public disclosure (coordinated disclosure).

## Supported Versions

| Branch | Spring Boot line | Status |
|--------|------------------|--------|
| 2.1.x  | 2.1.x            | Security fixes |
| 2.0.x  | 2.0.x            | Security fixes (best effort; Spring Boot 2.0.x is EOL upstream) |
| 1.5.x  | 1.5.x            | Not maintained |

> Spring Boot 2.0.x and 2.1.x are past their upstream OSS support windows. Consumers should
> plan migration to a supported Spring Boot line; see the remediation roadmap below.

## Secure Configuration Requirements (consumers)

### 1. Redis entity cache — untrusted deserialization (CWE-502) — **required hardening**

The optional Redis entity-cache path (`org.springframework.data.requery.cache.redis`)
deserializes cached objects. `RedisSerializedEntity` implements a custom Java
`readObject`, and Redisson's default codec (FST) performs unrestricted, Java-compatible
deserialization. An attacker able to **write to the Redis instance** or **man-in-the-middle
the Redis connection** could plant a crafted serialized payload that executes a gadget chain
on the application classpath (remote code execution).

If you enable the Redis cache, you **must**:

1. **Authenticate Redis** — never run against an unauthenticated instance. Do **not** rely
   on the `password: null` value shown in the sample `redisson-jcache.yaml`.
2. **Encrypt the channel** — use TLS (`rediss://`) between the application and Redis.
3. **Network-isolate** Redis so it is reachable only by the owning application; never place
   it on a shared/multi-tenant or internet-exposed network.
4. **Apply a JVM deserialization allow-list.** On JDK 8u121+/9+ set a serialization filter
   limiting deserialization to your entity/JDK types, e.g.:
   ```
   -Djdk.serialFilter=com.yourco.domain.**;java.**;!*
   ```
   (JEP-290 / `ObjectInputFilter`.) Deny-by-default (`!*`) and allow only what you need.
5. Prefer the in-heap **Cache2k** cache path where possible — it stores live object
   references and does **not** deserialize untrusted bytes.

A library-side remediation (schema-bound, non-executable codec and/or a built-in
`ObjectInputFilter` allow-list) is tracked in the roadmap below.

### 2. Native / `@Query` and derived queries

The library binds user-supplied argument **values** as JDBC `PreparedStatement` parameters
(positional `?`), including for derived queries and Query-By-Example — this is safe against
SQL injection. However:

- **Named parameters (`:name`) are not supported** in the raw path (positional `?` only).
  Do **not** work around this by building `@Query` strings or `default`-method queries from
  concatenated untrusted input — that reintroduces SQL injection in your code. Keep all
  user values in bound parameters.

### 3. Schema creation and credentials

- Set `spring.data.requery.tableCreationMode` deliberately; do **not** enable
  destructive/auto-create schema modes in production.
- Never ship default credentials. The `sa/sa` H2 values in the demo module are for local
  development only. Supply real, secret-managed credentials in every non-local environment,
  and do not expose the H2 console.

## Dependency Baseline & Known-Vulnerable Components (Finding 2)

Consumers inherit this library's transitive dependencies. The project pins versions
centrally in `build.gradle` (`dependencyManagement`) and `gradle.properties`.

Addressed in the current dependency-hygiene change:

| Component | From | To | CVE(s) closed |
|-----------|------|----|---------------|
| logback-classic | 1.2.3 | 1.2.13 | CVE-2021-42550, CVE-2023-6378 |
| snappy-java | 1.1.7.1 | 1.1.10.5 | CVE-2023-34453, CVE-2023-34455, CVE-2023-43642 |
| mysql-connector-java | 8.0.11 | 8.0.28 | CVE-2021-2471, CVE-2019-2692 |
| postgresql (JDBC) | 42.2.5 | 42.2.28 | CVE-2020-13692, CVE-2022-21724 |
| h2 (test/demo) | 1.4.197 | 2.2.224 | CVE-2021-42392, CVE-2022-23221 |
| slf4j-api | 1.7.25 | 1.7.36 | — (currency) |
| javassist | 3.22.0-GA | 3.29.2-GA | — (currency) |
| commons-collections4 | 4.1 | 4.4 | — (4.1 already mitigates CVE-2015-6420/-7501) |

> **H2 note:** H2 2.x has SQL-compatibility changes versus 1.4.x. The test suite must be
> revalidated in CI after this bump (and `test { ignoreFailures true }` should be removed so
> regressions actually fail the build).

## Remediation Roadmap (tracked follow-ups)

These require larger, separately-reviewed changes and are **not** included in the
dependency-hygiene change:

1. **Finding 1 code fix (CWE-502):** replace Java-native/FST serialization in the Redis
   cache with a schema-bound, non-executable codec (JSON/Protobuf with explicit type
   mapping) and/or install a built-in `ObjectInputFilter` allow-list in
   `RedisSerializedEntity`. Until then, the deployment hardening above is mandatory.
2. **Spring Framework / Spring Boot upgrade (CVE-2022-22965 "Spring4Shell",
   CVE-2016-1000027):** migrate to a Spring Boot line carrying Spring Framework 5.2.20+/
   5.3.18+. This breaks the current "Spring Boot 2.0.x" contract and is planned on a
   forward-looking branch.
3. **Build/supply-chain hardening:** remove `test { ignoreFailures true }`; sign published
   artifacts; publish an SBOM (CycloneDX); add SCA gating (OWASP Dependency-Check / OSV /
   Renovate) to CI; migrate off the retired jcenter repository.
