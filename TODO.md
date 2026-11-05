# TODO — Security & Dependency Hygiene

Generated: 2026-08-26  
Branch: `fix/jackson-2.22-upgrade`

---

## Security Scan Results

**CVE scan date:** 2026-08-26  
**Result:** ✅ No known CVEs found in any direct or transitive dependency.

---

## Dependency Hygiene Findings

The items below are not CVEs today but represent outdated or end-of-life libraries
that carry long-term security and maintenance risk.

### 🔴 High Priority

#### 1. `io.swagger.core.v3:swagger-models` — version mismatch
- **Managed version:** `2.2.0`
- **Effective version (transitive via `swagger-parser:2.1.19`):** `2.2.19`
- **Issue:** The pinned version in `dependencyManagement` is 19 minor releases behind the
  version already on the classpath. This means `swagger-models` is never actually
  overriding the transitive resolution and gives a false sense of control.
- **Fix:** Update `swagger-models` version in `pom.xml` `dependencyManagement` to `2.2.19`.
- **File:** `pom.xml`

---

### 🟠 Medium Priority

#### 2. `org.apache.httpcomponents:httpclient` — EOL library
- **Managed version:** `4.5.14`
- **Issue:** Apache HttpClient 4.x is **end-of-life**. The successor is
  `org.apache.hc.client5:httpclient5` (HttpClient 5.x), which is already used in
  `modules/apim-cli-tests`. No CVE at 4.5.14 today, but the 4.x line will not receive
  security patches.
- **Fix:** Migrate usages in `modules/apim-adapter` and `modules/apis` from
  `org.apache.httpcomponents:httpclient` to `org.apache.hc.client5:httpclient5`.
- **Files:** `pom.xml`, `modules/apim-adapter/pom.xml`, `modules/apis/pom.xml`

#### ~~3. `org.apache.logging.log4j:log4j-slf4j-impl` — behind current release~~ ✅ DONE
- **Was:** `log4j-slf4j-impl:2.20.0` + `slf4j-api:1.7.36`
- **Now:** `log4j-slf4j2-impl:2.25.4` + `log4j-api/core:2.25.4` + `slf4j-api:2.0.18`
- `log4j-slf4j-impl` (SLF4J 1.x bridge) was discontinued at 2.20.0; replaced with
  `log4j-slf4j2-impl` (SLF4J 2.x bridge). Source unchanged — only `Logger`/`LoggerFactory`
  used, both backward-compatible in SLF4J 2.x.

---

### 🟡 Low Priority

#### 4. `org.slf4j:slf4j-api` — legacy branch
- **Managed version:** `1.7.36`
- **Current release:** `2.0.x`
- **Issue:** SLF4J 1.x is the legacy branch. 2.x has been stable for over a year. No CVE
  at 1.7.36.
- **Fix:** Upgrade to `slf4j-api:2.0.x` (requires aligning `log4j-slf4j-impl` →
  `log4j-slf4j2-impl` artifact rename).
- **File:** `pom.xml`

#### 5. `org.glassfish.jaxb:jaxb-runtime` — dated release
- **Managed version:** `2.3.9`
- **Current release:** `4.x` (under Eclipse EE4J)
- **Issue:** The 2.3.x line is a legacy Jakarta EE 8 artefact. No CVE at 2.3.9.
- **Fix:** Evaluate upgrade to `4.0.x` (requires Jakarta EE 10 namespace migration:
  `javax.*` → `jakarta.*`).
- **File:** `pom.xml`

#### 6. `commons-logging:commons-logging` — transitive, slightly outdated
- **Effective version (transitive):** `1.2`
- **Current release:** `1.3.x`
- **Issue:** Pulled transitively via `httpclient:4.5.14`. No CVE at 1.2.
- **Fix:** Add `commons-logging:commons-logging:1.3.x` to `dependencyManagement` to force
  the newer version, or resolve as part of the HttpClient 5.x migration (item #2 above).
- **File:** `pom.xml`

