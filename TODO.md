# TODO — Security & Dependency Hygiene

Generated: 2026-08-26  
Last reviewed: 2026-11-05  
Branch: `fix/jackson-2.22-upgrade`

---

## Security Scan Results

**CVE scan date:** 2026-08-26  
**Result:** ✅ No known CVEs found in any direct or transitive dependency.

**Update 2026-11-05:** Jackson was bumped from `2.22.2` to `2.22.4` (`d2a17fa6`) to pick up the
latest security fixes.

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

#### ~~2. `org.apache.httpcomponents:httpclient` — EOL library~~ ✅ DONE
- **Was:** `httpclient:4.5.14` (HttpClient 4.x is end-of-life)
- **Now:** `org.apache.hc.client5:httpclient5:5.6.4`, used by `modules/apim-adapter`, `modules/apis`
  and `modules/apim-cli-tests`. No `org.apache.http.*` imports remain (commit `30a39167`).

#### ~~3. `org.apache.logging.log4j:log4j-slf4j-impl` — behind current release~~ ✅ DONE
- **Was:** `log4j-slf4j-impl:2.20.0` + `slf4j-api:1.7.36`
- **Now:** `log4j-slf4j2-impl` + `log4j-api/core:2.26.1` + `slf4j-api:2.0.20`
- `log4j-slf4j-impl` (SLF4J 1.x bridge) was discontinued at 2.20.0; replaced with
  `log4j-slf4j2-impl` (SLF4J 2.x bridge). Source unchanged — only `Logger`/`LoggerFactory`
  used, both backward-compatible in SLF4J 2.x.

---

### 🟡 Low Priority

#### ~~4. `org.slf4j:slf4j-api` — legacy branch~~ ✅ DONE
- **Was:** `slf4j-api:1.7.36`
- **Now:** `slf4j-api:2.0.20` together with `log4j-slf4j2-impl` (done as part of item 3).

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
- **Update:** The HttpClient 5.x migration (item #2) removed `httpclient:4.5.14`, so this path no longer
  applies. Run `mvn dependency:tree -Dincludes=commons-logging` to confirm it is gone from the
  classpath, then close this item. If another dependency still pulls it in, add
  `commons-logging:commons-logging:1.3.x` to `dependencyManagement`.
- **File:** `pom.xml`

