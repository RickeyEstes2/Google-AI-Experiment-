package com.example.solveflow.data.templates

import com.example.solveflow.data.model.FlowBranch
import com.example.solveflow.data.model.FlowNode
import com.example.solveflow.data.model.FlowchartData
import com.example.solveflow.data.model.NodeType

object DefaultFlowcharts {

    val networkTroubleshooting = FlowchartData(
        id = "tpl_network_debug",
        title = "Network & Internet Troubleshooting",
        description = "Systematic diagnostic workflow to isolate and resolve network drops, DNS failures, and gateway issues.",
        category = "IT & Infrastructure",
        rootNodeId = "net_1",
        isTemplate = true,
        nodes = listOf(
            FlowNode(
                id = "net_1",
                type = NodeType.START,
                title = "Device has no internet access",
                description = "Symptoms: Web pages fail to load, connection icons indicate offline or limited connectivity.",
                branches = listOf(
                    FlowBranch("b1", "Begin Diagnosis", "net_2")
                ),
                stepNumber = 1
            ),
            FlowNode(
                id = "net_2",
                type = NodeType.DECISION,
                title = "Can you ping the local default gateway (e.g. 192.168.1.1)?",
                description = "Open terminal/command prompt and run 'ping <gateway_ip>'.",
                branches = listOf(
                    FlowBranch("b2_yes", "Yes (Gateway responds)", "net_3"),
                    FlowBranch("b2_no", "No (Gateway unreachable)", "net_4")
                ),
                stepNumber = 2
            ),
            FlowNode(
                id = "net_3",
                type = NodeType.DECISION,
                title = "Can you ping public IP 8.8.8.8 but domain names fail?",
                description = "Tests if packet routing works but DNS resolution is broken.",
                branches = listOf(
                    FlowBranch("b3_yes", "Yes (Ping IP works, DNS fails)", "net_5"),
                    FlowBranch("b3_no", "No (Both IP and DNS fail)", "net_6")
                ),
                stepNumber = 3
            ),
            FlowNode(
                id = "net_4",
                type = NodeType.ACTION,
                title = "Inspect Physical Connection & DHCP Lease",
                description = "Check Ethernet cable latch or WiFi SSID. Verify if device has an APIPA address (169.254.x.x). Run 'ipconfig /renew' or toggle WiFi adapter.",
                branches = listOf(
                    FlowBranch("b4_fixed", "IP Assigned & Gateway reachable", "net_2"),
                    FlowBranch("b4_router", "Still unreachable", "net_7")
                ),
                stepNumber = 4
            ),
            FlowNode(
                id = "net_5",
                type = NodeType.ACTION,
                title = "Flush DNS and Set Reliable Resolver",
                description = "Run 'ipconfig /flushdns'. Switch adapter DNS server manually to 1.1.1.1 (Cloudflare) or 8.8.8.8 (Google Public DNS).",
                branches = listOf(
                    FlowBranch("b5_fixed", "Websites load normally", "net_resolved"),
                    FlowBranch("b5_captive", "Still failing / Redirected", "net_8")
                ),
                stepNumber = 5
            ),
            FlowNode(
                id = "net_6",
                type = NodeType.DECISION,
                title = "Are other devices on the same local network offline?",
                description = "Check colleagues' laptops, smartphones, or network monitors.",
                branches = listOf(
                    FlowBranch("b6_all", "Yes (All devices affected)", "net_7"),
                    FlowBranch("b6_only_me", "No (Only this device)", "net_9")
                ),
                stepNumber = 6
            ),
            FlowNode(
                id = "net_7",
                type = NodeType.ACTION,
                title = "Power Cycle Router & Modem",
                description = "Disconnect power from router and ONT/modem for 30 seconds. Plug modem in first, wait 2 mins, then power on router. Check WAN light status.",
                branches = listOf(
                    FlowBranch("b7_ok", "WAN light solid, devices reconnect", "net_resolved"),
                    FlowBranch("b7_isp", "WAN light flashing or red", "net_isp_escalate")
                ),
                stepNumber = 7
            ),
            FlowNode(
                id = "net_8",
                type = NodeType.ACTION,
                title = "Check for Captive Portal / VPN Block",
                description = "Open http://neverssl.com to trigger captive portal login. Disable active VPN or corporate proxy tunnels temporarily.",
                branches = listOf(
                    FlowBranch("b8_solved", "Portal authenticated / VPN resolved", "net_resolved"),
                    FlowBranch("b8_fail", "Security certificate error", "net_sec_escalate")
                ),
                stepNumber = 8
            ),
            FlowNode(
                id = "net_9",
                type = NodeType.ACTION,
                title = "Reset Device Network Stack & Drivers",
                description = "Run 'netsh int ip reset' and reinstall network adapter driver in Device Manager. Check for conflicting 3rd-party antivirus firewalls.",
                branches = listOf(
                    FlowBranch("b9_ok", "Connection restored", "net_resolved"),
                    FlowBranch("b9_hardware", "Hardware adapter error persists", "net_hw_escalate")
                ),
                stepNumber = 9
            ),
            FlowNode(
                id = "net_resolved",
                type = NodeType.OUTCOME_SUCCESS,
                title = "Problem Resolved: Network Connectivity Restored",
                description = "Internet connectivity verified. Latency and DNS resolution functioning within normal SLA.",
                branches = emptyList(),
                stepNumber = 10
            ),
            FlowNode(
                id = "net_isp_escalate",
                type = NodeType.OUTCOME_ESCALATE,
                title = "Escalate to ISP: Fiber/Cable Line Outage",
                description = "WAN signal failure detected upstream. Call ISP with modem MAC address and check regional outage status dashboard.",
                branches = emptyList(),
                stepNumber = 11
            ),
            FlowNode(
                id = "net_sec_escalate",
                type = NodeType.OUTCOME_ESCALATE,
                title = "Escalate to SecOps / IT Admin",
                description = "Possible 802.1X enterprise certificate expiration, rogue proxy, or firewall policy block.",
                branches = emptyList(),
                stepNumber = 12
            ),
            FlowNode(
                id = "net_hw_escalate",
                type = NodeType.OUTCOME_ESCALATE,
                title = "Hardware Failure: Replace Network Card / Cable",
                description = "NIC hardware or physical patch panel port failure suspected. Try USB-to-Ethernet adapter.",
                branches = emptyList(),
                stepNumber = 13
            )
        )
    )

    val productionIncidentRCA = FlowchartData(
        id = "tpl_prod_incident",
        title = "Production Service High Latency & Outage",
        description = "DevOps/SRE triage workflow when api p99 latency spikes or error rates exceed SLO threshold.",
        category = "Software & SRE",
        rootNodeId = "prod_1",
        isTemplate = true,
        nodes = listOf(
            FlowNode(
                id = "prod_1",
                type = NodeType.START,
                title = "Alert: HTTP 5xx rate > 2% or p99 Latency > 1500ms",
                description = "PagerDuty fired. Multiple regions reporting degraded health checks.",
                branches = listOf(
                    FlowBranch("p1_triage", "Begin Incident Triage", "prod_2")
                ),
                stepNumber = 1
            ),
            FlowNode(
                id = "prod_2",
                type = NodeType.DECISION,
                title = "Was a code release or config change deployed within the last 30 minutes?",
                description = "Check deployment pipeline, Git tags, or feature flag dashboards.",
                branches = listOf(
                    FlowBranch("p2_recent_deploy", "Yes (Recent deployment)", "prod_3"),
                    FlowBranch("p2_no_deploy", "No (No recent changes)", "prod_4")
                ),
                stepNumber = 2
            ),
            FlowNode(
                id = "prod_3",
                type = NodeType.ACTION,
                title = "Initiate Immediate Canary / Service Rollback",
                description = "Roll back to previous stable release image immediately. Stop automated rollout.",
                branches = listOf(
                    FlowBranch("p3_recovered", "Metrics normalize to healthy", "prod_resolved_rollback"),
                    FlowBranch("p3_still_degraded", "Metrics remain degraded", "prod_4")
                ),
                stepNumber = 3
            ),
            FlowNode(
                id = "prod_4",
                type = NodeType.DECISION,
                title = "Are database connection pools exhausted or CPU at 100%?",
                description = "Inspect RDS / Cloud SQL metrics, active query count, and lock wait graph.",
                branches = listOf(
                    FlowBranch("p4_db_choke", "Yes (Database throttled)", "prod_5"),
                    FlowBranch("p4_app_cpu", "No (Database healthy)", "prod_6")
                ),
                stepNumber = 4
            ),
            FlowNode(
                id = "prod_5",
                type = NodeType.ACTION,
                title = "Kill Long-Running Unindexed Query & Increase Pool Limits",
                description = "Identify blocking transaction in pg_stat_activity or SHOW PROCESSLIST. Terminate runaway query and activate read replica pooling.",
                branches = listOf(
                    FlowBranch("p5_cleared", "Database CPU drops, queues clear", "prod_resolved_db"),
                    FlowBranch("p5_ddos", "High traffic flood continues", "prod_escalate_ddos")
                ),
                stepNumber = 5
            ),
            FlowNode(
                id = "prod_6",
                type = NodeType.DECISION,
                title = "Is ingress traffic spike 3x above average baseline?",
                description = "Review Cloudflare / WAF logs, DDoS protection metrics, and rate limit counters.",
                branches = listOf(
                    FlowBranch("p6_traffic_burst", "Yes (Abnormal traffic surge)", "prod_7"),
                    FlowBranch("p6_downstream", "No (Normal traffic volume)", "prod_8")
                ),
                stepNumber = 6
            ),
            FlowNode(
                id = "prod_7",
                type = NodeType.ACTION,
                title = "Enable Under Attack Mode & Auto-scale Cluster Pods",
                description = "Switch Cloudflare to JS challenge on suspect endpoints. Scale horizontal pod autoscaler (HPA) minimum replicas from 10 to 40.",
                branches = listOf(
                    FlowBranch("p7_stable", "Latency stabilizes under load", "prod_resolved_scale"),
                    FlowBranch("p7_upstream", "Suspect malicious botnet", "prod_escalate_ddos")
                ),
                stepNumber = 7
            ),
            FlowNode(
                id = "prod_8",
                type = NodeType.ACTION,
                title = "Inspect External 3rd-Party Dependencies",
                description = "Check Payment Gateway, Auth0, or Cloud Storage status pages. Enable circuit breaker fail-soft mode.",
                branches = listOf(
                    FlowBranch("p8_fallback", "Circuit breaker fallback active", "prod_resolved_circuit"),
                    FlowBranch("p8_hard_down", "Vendor hard down with no fallback", "prod_escalate_vendor")
                ),
                stepNumber = 8
            ),
            FlowNode(
                id = "prod_resolved_rollback",
                type = NodeType.OUTCOME_SUCCESS,
                title = "Incident Mitigated via Rollback",
                description = "Service returned to baseline. Schedule post-mortem on buggy deployment commit.",
                branches = emptyList(),
                stepNumber = 9
            ),
            FlowNode(
                id = "prod_resolved_db",
                type = NodeType.OUTCOME_SUCCESS,
                title = "Incident Mitigated via DB Optimization",
                description = "Blocking query terminated. Add missing index to backlog before next migration.",
                branches = emptyList(),
                stepNumber = 10
            ),
            FlowNode(
                id = "prod_resolved_scale",
                type = NodeType.OUTCOME_SUCCESS,
                title = "Incident Mitigated via Autoscaling & WAF",
                description = "Cluster expanded to absorb load. WAF rules blocking scrape bots.",
                branches = emptyList(),
                stepNumber = 11
            ),
            FlowNode(
                id = "prod_resolved_circuit",
                type = NodeType.OUTCOME_SUCCESS,
                title = "Incident Mitigated via Circuit Breaker",
                description = "Graceful degradation enabled. Core user workflow intact.",
                branches = emptyList(),
                stepNumber = 12
            ),
            FlowNode(
                id = "prod_escalate_ddos",
                type = NodeType.OUTCOME_ESCALATE,
                title = "Escalate to Security Incident Response (P1)",
                description = "Volumetric DDoS or credential stuffing in progress. Engage CDN emergency defense team.",
                branches = emptyList(),
                stepNumber = 13
            ),
            FlowNode(
                id = "prod_escalate_vendor",
                type = NodeType.OUTCOME_ESCALATE,
                title = "Post Incident Status Page: 3rd Party Vendor Down",
                description = "Publish status banner on statuspage.io. Notify customer support leads.",
                branches = emptyList(),
                stepNumber = 14
            )
        )
    )

    val fiveWhysRca = FlowchartData(
        id = "tpl_five_whys",
        title = "5-Whys Root Cause Analysis (Database OOM)",
        description = "Classic Six Sigma / Lean engineering root cause discovery flowchart identifying the underlying procedural failure.",
        category = "Root Cause Analysis",
        rootNodeId = "why_1",
        isTemplate = true,
        nodes = listOf(
            FlowNode(
                id = "why_1",
                type = NodeType.START,
                title = "Problem Statement: Database server ran out of memory and terminated.",
                description = "The database process was killed by Linux OOM killer at 03:14 AM.",
                branches = listOf(
                    FlowBranch("w1_next", "Why #1: Why was the process killed?", "why_2")
                ),
                stepNumber = 1
            ),
            FlowNode(
                id = "why_2",
                type = NodeType.DECISION,
                title = "Why #1: Was memory consumed by heap leak or a single query?",
                description = "Analyze memory consumption timeline leading up to 03:14 AM.",
                branches = listOf(
                    FlowBranch("w2_query", "Single massive unpaged export query", "why_3"),
                    FlowBranch("w2_leak", "Gradual buffer cache leak over weeks", "why_2b")
                ),
                stepNumber = 2
            ),
            FlowNode(
                id = "why_2b",
                type = NodeType.ACTION,
                title = "Inspect OS Dirty Page Flusher & Buffer Configuration",
                description = "Review vm.dirty_ratio, sysctl parameters, and connection pool sizing.",
                branches = listOf(
                    FlowBranch("w2b_tune", "Tuned OS parameters", "why_root_config")
                ),
                stepNumber = 3
            ),
            FlowNode(
                id = "why_3",
                type = NodeType.DECISION,
                title = "Why #2: Why did an unpaged query load 12 million records into RAM?",
                description = "Check client application endpoint and payload parameters.",
                branches = listOf(
                    FlowBranch("w3_limit", "No pagination or row limit requested by client", "why_4"),
                    FlowBranch("w3_batch", "Batch job ran concurrently with ETL", "why_3b")
                ),
                stepNumber = 4
            ),
            FlowNode(
                id = "why_3b",
                type = NodeType.ACTION,
                title = "Re-schedule batch tasks to off-peak hours",
                description = "Separate analytical workloads onto isolated read-only replicas.",
                branches = listOf(
                    FlowBranch("w3b_done", "Replicas configured", "why_root_architecture")
                ),
                stepNumber = 5
            ),
            FlowNode(
                id = "why_4",
                type = NodeType.DECISION,
                title = "Why #3: Why was the API allowed to query without mandatory pagination limits?",
                description = "Inspect API contract and ORM query specifications.",
                branches = listOf(
                    FlowBranch("w4_api", "API schema lacked max_limit validation rule", "why_5")
                ),
                stepNumber = 6
            ),
            FlowNode(
                id = "why_5",
                type = NodeType.DECISION,
                title = "Why #4: Why was this endpoint merged without schema boundary checks?",
                description = "Inspect pull request review checklist and test coverage.",
                branches = listOf(
                    FlowBranch("w5_review", "PR review checklist did not mandate query boundary test", "why_6")
                ),
                stepNumber = 7
            ),
            FlowNode(
                id = "why_6",
                type = NodeType.ACTION,
                title = "Why #5 (Root Cause): Why is query limit validation not enforced automatically in CI?",
                description = "There was no static analysis rule or database guardrail in CI to catch unrestricted SELECT statements.",
                branches = listOf(
                    FlowBranch("w6_fix", "Implement automated CI linter & DB query limits", "why_root_resolved")
                ),
                stepNumber = 8
            ),
            FlowNode(
                id = "why_root_resolved",
                type = NodeType.OUTCOME_SUCCESS,
                title = "Systemic Fix Implemented",
                description = "1. Added default LIMIT 500 at ORM middleware level. 2. Added CI linter checking repository queries. 3. Enforced PR test coverage for large datasets.",
                branches = emptyList(),
                stepNumber = 9
            ),
            FlowNode(
                id = "why_root_config",
                type = NodeType.OUTCOME_SUCCESS,
                title = "Kernel & Memory Guardrails Applied",
                description = "Configured cgroup memory limits and tuned vm.swappiness and overcommit_memory.",
                branches = emptyList(),
                stepNumber = 10
            ),
            FlowNode(
                id = "why_root_architecture",
                type = NodeType.OUTCOME_SUCCESS,
                title = "Architectural Segregation Completed",
                description = "Heavy analytics isolated from transactional OLTP workload.",
                branches = emptyList(),
                stepNumber = 11
            )
        )
    )

    val hardwareDiagnostics = FlowchartData(
        id = "tpl_hardware_power",
        title = "Hardware / Device Power Failure Diagnostics",
        description = "Hardware troubleshooting guide for devices that fail to turn on, charge, or boot.",
        category = "Hardware & Electronics",
        rootNodeId = "hw_1",
        isTemplate = true,
        nodes = listOf(
            FlowNode(
                id = "hw_1",
                type = NodeType.START,
                title = "Device does not turn on or show signs of life",
                description = "Screen remains black, no LED indicators, no vibration or fan spin upon pressing power.",
                branches = listOf(
                    FlowBranch("h1_start", "Start Diagnostic Check", "hw_2")
                ),
                stepNumber = 1
            ),
            FlowNode(
                id = "hw_2",
                type = NodeType.DECISION,
                title = "When plugged into a known-good charger, does charging LED illuminate?",
                description = "Use verified working cable, wall adapter, and power outlet.",
                branches = listOf(
                    FlowBranch("h2_led_on", "Yes (LED turns on or blinks)", "hw_3"),
                    FlowBranch("h2_no_led", "No (Zero LED activity)", "hw_4")
                ),
                stepNumber = 2
            ),
            FlowNode(
                id = "hw_3",
                type = NodeType.ACTION,
                title = "Allow 20 Minutes Deep Charging then Force Hard Reset",
                description = "Depleted lithium batteries require trickle charge before boot. Hold Power + Volume Down for 15 seconds.",
                branches = listOf(
                    FlowBranch("h3_boots", "Device vibrates and boots logo", "hw_resolved"),
                    FlowBranch("h3_loop", "Stuck in boot loop", "hw_bootloop")
                ),
                stepNumber = 3
            ),
            FlowNode(
                id = "hw_4",
                type = NodeType.DECISION,
                title = "Inspect Charging Port: Is debris, lint, or pin damage visible?",
                description = "Shine flashlight into USB/charging port.",
                branches = listOf(
                    FlowBranch("h4_debris", "Yes (Dust/lint compacted inside)", "hw_5"),
                    FlowBranch("h4_clean", "No (Port is clean and firm)", "hw_6")
                ),
                stepNumber = 4
            ),
            FlowNode(
                id = "hw_5",
                type = NodeType.ACTION,
                title = "Carefully Clear Debris Using Wooden / Plastic Pick",
                description = "Never use metal needles. Gently scrape compacted pocket lint from port bottom and blow with compressed air. Re-test charger.",
                branches = listOf(
                    FlowBranch("h5_working", "Charger clicks into place and charges", "hw_resolved"),
                    FlowBranch("h5_failed", "Still no charging current", "hw_6")
                ),
                stepNumber = 5
            ),
            FlowNode(
                id = "hw_6",
                type = NodeType.DECISION,
                title = "Connect USB Power Meter: Does device draw any current (> 0.05A)?",
                description = "Measures current draw from 5V/9V charging rail.",
                branches = listOf(
                    FlowBranch("h6_short", "0.00A (Open circuit / blown fuse)", "hw_escalate_fuse"),
                    FlowBranch("h6_current", "Normal current draw (0.5A - 2.0A)", "hw_display_dead")
                ),
                stepNumber = 6
            ),
            FlowNode(
                id = "hw_resolved",
                type = NodeType.OUTCOME_SUCCESS,
                title = "Device Restored to Working Order",
                description = "Power and boot sequences operational. Battery health verified.",
                branches = emptyList(),
                stepNumber = 7
            ),
            FlowNode(
                id = "hw_bootloop",
                type = NodeType.ACTION,
                title = "Boot into Recovery Mode & Wipe Cache / Safe Mode",
                description = "Hold recovery button combination. Clear system cache partition or flash stock firmware.",
                branches = listOf(
                    FlowBranch("h_bl_fixed", "Boots into OS cleanly", "hw_resolved"),
                    FlowBranch("h_bl_nand", "Internal eMMC/NAND storage corrupt", "hw_escalate_board")
                ),
                stepNumber = 8
            ),
            FlowNode(
                id = "hw_display_dead",
                type = NodeType.OUTCOME_ESCALATE,
                title = "Backlight or Display Panel Dead (Mainboard Alive)",
                description = "Device draws power and rings/vibrates, but screen is dark. Replace OLED/LCD assembly.",
                branches = emptyList(),
                stepNumber = 9
            ),
            FlowNode(
                id = "hw_escalate_fuse",
                type = NodeType.OUTCOME_ESCALATE,
                title = "Board-Level Failure: Blown Charging IC or Fuse",
                description = "Power management IC (PMIC) or shorted filter capacitor on VBUS. Requires microsoldering repair.",
                branches = emptyList(),
                stepNumber = 10
            ),
            FlowNode(
                id = "hw_escalate_board",
                type = NodeType.OUTCOME_ESCALATE,
                title = "Hardware Failure: Replace Logic Board",
                description = "Persistent flash controller failure or CPU solder detachment.",
                branches = emptyList(),
                stepNumber = 11
            )
        )
    )

    val customerIncidentTriage = FlowchartData(
        id = "tpl_incident_triage",
        title = "Critical Incident & SLA Ticket Triage",
        description = "Operations response matrix to categorize severity, communicate status, and route customer impacting incidents.",
        category = "Operations & Support",
        rootNodeId = "inc_1",
        isTemplate = true,
        nodes = listOf(
            FlowNode(
                id = "inc_1",
                type = NodeType.START,
                title = "Inbound Incident / Urgent Customer Escalation",
                description = "Customer reports blocking malfunction or system unavailability.",
                branches = listOf(
                    FlowBranch("inc1_eval", "Evaluate Impact & Scope", "inc_2")
                ),
                stepNumber = 1
            ),
            FlowNode(
                id = "inc_2",
                type = NodeType.DECISION,
                title = "Is core revenue or primary business function completely blocked?",
                description = "Examples: Checkout broken, full data loss risk, complete outage for enterprise accounts.",
                branches = listOf(
                    FlowBranch("inc2_sev1", "Yes (Complete blocker)", "inc_sev1"),
                    FlowBranch("inc2_sev2", "No (Partial degradation or workaround exists)", "inc_3")
                ),
                stepNumber = 2
            ),
            FlowNode(
                id = "inc_sev1",
                type = NodeType.ACTION,
                title = "Declare SEV-1: Open War Room & Send 15-Min Stakeholder Update",
                description = "Page on-call incident commander, lead engineer, and customer success manager. Post initial incident acknowledged banner.",
                branches = listOf(
                    FlowBranch("inc_war_room", "Investigate Immediate Workaround", "inc_4")
                ),
                stepNumber = 3
            ),
            FlowNode(
                id = "inc_3",
                type = NodeType.DECISION,
                title = "Does this affect > 10% of users or a Tier-1 Enterprise contract?",
                description = "Assess customer tier and aggregate error volume.",
                branches = listOf(
                    FlowBranch("inc3_sev2", "Yes (High impact / SEV-2)", "inc_sev2"),
                    FlowBranch("inc3_sev3", "No (Moderate or isolated / SEV-3)", "inc_sev3")
                ),
                stepNumber = 4
            ),
            FlowNode(
                id = "inc_sev2",
                type = NodeType.ACTION,
                title = "Assign SEV-2: Target 2-Hour Resolution SLA",
                description = "Route directly to domain engineering sprint. Provide hourly customer updates.",
                branches = listOf(
                    FlowBranch("inc_s2_fix", "Engineers formulate patch", "inc_4")
                ),
                stepNumber = 5
            ),
            FlowNode(
                id = "inc_sev3",
                type = NodeType.ACTION,
                title = "Assign SEV-3: Standard Priority Queue",
                description = "Log reproduction steps, capture customer tenant logs, and prioritize for next release cycle.",
                branches = listOf(
                    FlowBranch("inc_s3_done", "Ticket prioritized", "inc_resolved_queued")
                ),
                stepNumber = 6
            ),
            FlowNode(
                id = "inc_4",
                type = NodeType.DECISION,
                title = "Can a feature flag, DNS failover, or bypass configuration restore service?",
                description = "Assess temporary operational mitigation without code deployment.",
                branches = listOf(
                    FlowBranch("inc4_mitigate", "Yes (Workaround verified)", "inc_mitigated"),
                    FlowBranch("inc4_patch", "No (Requires hotfix build & QA test)", "inc_hotfix")
                ),
                stepNumber = 7
            ),
            FlowNode(
                id = "inc_mitigated",
                type = NodeType.OUTCOME_SUCCESS,
                title = "Incident Mitigated with Operational Bypass",
                description = "Customer restored via config bypass. Permanent fix scheduled for standard CI deployment.",
                branches = emptyList(),
                stepNumber = 8
            ),
            FlowNode(
                id = "inc_hotfix",
                type = NodeType.ACTION,
                title = "Execute Expedited Hotfix Pipeline & Emergency Deploy",
                description = "Peer review emergency PR with 2 senior approvers. Fast-track regression tests and deploy behind canary.",
                branches = listOf(
                    FlowBranch("inc_deploy_ok", "Verification tests pass in prod", "inc_resolved_hotfix"),
                    FlowBranch("inc_deploy_fail", "Hotfix failed validation", "inc_escalate_exec")
                ),
                stepNumber = 9
            ),
            FlowNode(
                id = "inc_resolved_hotfix",
                type = NodeType.OUTCOME_SUCCESS,
                title = "Incident Closed: Production Hotfix Verified",
                description = "All health checks green. Customer notified of complete resolution. Trigger 48-hour retrospective.",
                branches = emptyList(),
                stepNumber = 10
            ),
            FlowNode(
                id = "inc_resolved_queued",
                type = NodeType.OUTCOME_SUCCESS,
                title = "Incident Triage Complete: Scheduled in Backlog",
                description = "Customer provided with temporary guidance and tracking ticket ID.",
                branches = emptyList(),
                stepNumber = 11
            ),
            FlowNode(
                id = "inc_escalate_exec",
                type = NodeType.OUTCOME_ESCALATE,
                title = "Escalate to VP of Engineering & Legal / PR",
                description = "Outage duration exceeding contractual SLA breach limits. Prepare customer credit compensation plan.",
                branches = emptyList(),
                stepNumber = 12
            )
        )
    )

    val allTemplates = listOf(
        networkTroubleshooting,
        productionIncidentRCA,
        fiveWhysRca,
        hardwareDiagnostics,
        customerIncidentTriage
    )
}
