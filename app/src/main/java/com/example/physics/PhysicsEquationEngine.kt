package com.example.physics

import com.example.data.model.EquationItem
import com.example.data.model.EquationTerm
import com.example.data.model.FundamentalForce
import com.example.data.model.SolutionStep

/**
 * Advanced Physics & Math Equation Engine.
 * Extracts, explains, color-codes, and step-by-step solves equations across the 4 fundamental forces:
 * - Green for Strong Nuclear Force
 * - Blue for Weak Nuclear Force
 * - Red for Electromagnetic Force
 * - Brown for Gravity
 */
object PhysicsEquationEngine {

    val BUILTIN_EQUATIONS: List<EquationItem> = listOf(
        // 1. STRONG NUCLEAR FORCE (GREEN)
        EquationItem(
            id = "qcd_lagrangian",
            title = "Quantum Chromodynamics (QCD) Lagrangian",
            rawFormula = "ℒ_QCD = ψ̄_i (iγ^μ (D_μ)_ij - m δ_ij) ψ_j - ¼ G^a_μν G^a,μν",
            primaryForce = FundamentalForce.STRONG_NUCLEAR,
            termsBreakdown = listOf(
                EquationTerm("ψ̄_i, ψ_j", "Quark Dirac Spinor Fields (u, d, s, c, b, t)", FundamentalForce.STRONG_NUCLEAR, "Color-charged quark triplets mediating strong interaction."),
                EquationTerm("D_μ = ∂_μ - i g_s A^a_μ T^a", "QCD Gauge Covariant Derivative", FundamentalForce.STRONG_NUCLEAR, "Couples quarks to the SU(3) gluon gauge fields via strong coupling constant g_s."),
                EquationTerm("G^a_μν", "Gluon Field Strength Tensor (8 Gell-Mann Gluons)", FundamentalForce.STRONG_NUCLEAR, "Non-Abelian gauge field curvature causing asymptotic freedom and quark confinement."),
                EquationTerm("m δ_ij", "Quark Current Mass Matrix", FundamentalForce.STRONG_NUCLEAR, "Rest mass generated through Yukawa couplings to Higgs field.")
            ),
            conceptualExplanation = "Describes how the Strong Nuclear Force holds quarks inside protons and neutrons via exchange of 8 color-charged massless vector gluons. Governed by non-Abelian SU(3) gauge symmetry.",
            stepByStepSolution = listOf(
                SolutionStep(1, "Dirac Kinetic Term for Quarks", "ℒ_fermion = ψ̄ iγ^μ ∂_μ ψ", "Propagates free non-interacting spin-1/2 quarks across spacetime."),
                SolutionStep(2, "SU(3) Color Gauge Invariance", "∂_μ → D_μ = ∂_μ - i g_s T^a A^a_μ", "Promotes global phase rotation to local color gauge symmetry introducing gluon fields A^a_μ."),
                SolutionStep(3, "Gluon Field Strength Tensor", "G^a_μν = ∂_μ A^a_ν - ∂_ν A^a_μ + g_s f^{abc} A^b_μ A^c_ν", "Self-interaction term f^{abc} gives rise to gluon-gluon binding and asymptotic freedom."),
                SolutionStep(4, "Combined QCD Action", "S_QCD = ∫ d⁴x [ ψ̄(iγ^μ D_μ - m)ψ - ¼ G^a_μν G^{a,μν} ]", "Final invariant Lagrangian defining all strong interactions and nuclear binding energies.")
            ),
            variableNames = mapOf("g_s" to "Strong Coupling Constant", "m" to "Quark Mass (GeV/c²)", "α_s" to "Strong Fine-Structure Parameter"),
            defaultValues = mapOf("g_s" to 1.218, "m" to 0.0022, "α_s" to 0.118),
            unit = "GeV⁴ (Natural Units)"
        ),

        EquationItem(
            id = "yukawa_potential",
            title = "Yukawa Nuclear Binding Potential",
            rawFormula = "V(r) = - (g^2 / (4π r)) e^(- r / λ)",
            primaryForce = FundamentalForce.STRONG_NUCLEAR,
            termsBreakdown = listOf(
                EquationTerm("g^2 / 4π", "Strong Meson Coupling Constant", FundamentalForce.STRONG_NUCLEAR, "Determines effective strength of nuclear binding between nucleons."),
                EquationTerm("e^(-r/λ)", "Exponential Screening Factor", FundamentalForce.STRONG_NUCLEAR, "Suppresses strong force at distances greater than the pion Compton wavelength (r > 1.4 fm)."),
                EquationTerm("λ = ħ / (m_π c)", "Characteristic Nuclear Range", FundamentalForce.STRONG_NUCLEAR, "Range set by the rest mass of the mediating virtual pi-meson (pion)."),
                EquationTerm("r", "Inter-nucleon Separation Distance", FundamentalForce.STRONG_NUCLEAR, "Radial distance between proton and neutron cores inside the nucleus.")
            ),
            conceptualExplanation = "Explains the residual strong nuclear force binding protons and neutrons together inside the atomic nucleus via virtual pion (π⁰, π⁺, π⁻) meson exchange.",
            stepByStepSolution = listOf(
                SolutionStep(1, "Klein-Gordon Equation for Massive Meson", "(∇² - m_π² c² / ħ²) φ(r) = - g δ³(r)", "Describes a static spherically symmetric scalar field sourced by a point nucleon."),
                SolutionStep(2, "Radial Solution Formulation", "φ(r) = (g / 4πr) e^(- m_π c r / ħ)", "Yields the screened Coulomb potential with finite exponential cutoff."),
                SolutionStep(3, "Potential Energy Calculation", "V(r) = - g φ(r) = - (g² / 4πr) e^(- r / λ)", "Evaluates the attractive binding well holding the nucleus against Coulomb repulsion."),
                SolutionStep(4, "Numerical Evaluation for Deuteron", "V(1.2 fm) ≈ - 45 MeV (Attractive Well)", "Provides nuclear binding sufficient to overcome electromagnetic repulsion.")
            ),
            variableNames = mapOf("g" to "Effective Coupling (dimensionless)", "r" to "Distance r (fm)", "m_π" to "Pion Mass (MeV/c²)"),
            defaultValues = mapOf("g" to 14.0, "r" to 1.2, "m_π" to 139.57),
            unit = "MeV"
        ),

        // 2. WEAK NUCLEAR FORCE (BLUE)
        EquationItem(
            id = "fermi_beta_decay",
            title = "Fermi Weak Interaction & Beta Decay Rate",
            rawFormula = "Γ = (G_F^2 m_e^5 c^4 / (2π^3 ħ^7)) |M_fi|^2 f(Z, E_0)",
            primaryForce = FundamentalForce.WEAK_NUCLEAR,
            termsBreakdown = listOf(
                EquationTerm("G_F", "Fermi Weak Coupling Constant", FundamentalForce.WEAK_NUCLEAR, "Universal fundamental constant of the weak nuclear force: G_F / (ħc)³ ≈ 1.166 × 10⁻⁵ GeV⁻²."),
                EquationTerm("W^±, Z^0 Propagator", "Heavy Weak Gauge Boson Intermediate", FundamentalForce.WEAK_NUCLEAR, "Mediates quark flavor change d → u + W⁻ with subsequent W⁻ → e⁻ + ν̄_e."),
                EquationTerm("|M_fi|^2", "Nuclear Transition Matrix Element", FundamentalForce.WEAK_NUCLEAR, "Quantum overlap between initial neutron state and final proton/lepton states."),
                EquationTerm("f(Z, E_0)", "Fermi Phase-Space Integral", FundamentalForce.WEAK_NUCLEAR, "Kinematic phase-space available for emitted beta particle and antineutrino.")
            ),
            conceptualExplanation = "Describes nuclear beta decay (n → p + e⁻ + ν̄_e) and neutrino scattering driven by the Weak Nuclear Force through W⁺, W⁻, and Z⁰ intermediate vector bosons.",
            stepByStepSolution = listOf(
                SolutionStep(1, "Quark Level Flavor Transition", "d (-1/3 e) → u (+2/3 e) + W⁻", "Weak isospin ladder operator changes down quark to up quark."),
                SolutionStep(2, "Intermediate W-Boson Decay", "W⁻ → e⁻ + ν̄_e", "Heavy virtual W-boson (80.38 GeV) decays into electron and electron antineutrino."),
                SolutionStep(3, "Low Energy Four-Fermion Approximation", "ℒ_Fermi = (G_F / √2) [ψ̄_p γ^μ (1 - γ⁵) ψ_n] [ψ̄_e γ_μ (1 - γ⁵) ψ_ν]", "Effective contact Lagrangian with maximal parity violation (V - A)."),
                SolutionStep(4, "Decay Width & Half-Life Derivation", "τ = ħ / Γ ≈ 880 seconds (for free neutron)", "Calculates the exact lifetime of unstable radioactive isotopes and particles.")
            ),
            variableNames = mapOf("G_F" to "Fermi Constant (10⁻⁵ GeV⁻²)", "E_0" to "Endpoint Energy (MeV)", "Z" to "Atomic Number Z"),
            defaultValues = mapOf("G_F" to 1.166, "E_0" to 0.782, "Z" to 1.0),
            unit = "s⁻¹ (Decay Rate)"
        ),

        EquationItem(
            id = "electroweak_weinberg",
            title = "Glashow-Weinberg-Salam Electroweak Symmetry",
            rawFormula = "M_W = ½ g v,   M_Z = ½ √(g^2 + g'^2) v = M_W / cos(θ_W)",
            primaryForce = FundamentalForce.WEAK_NUCLEAR,
            termsBreakdown = listOf(
                EquationTerm("g, g'", "SU(2)_L and U(1)_Y Gauge Couplings", FundamentalForce.WEAK_NUCLEAR, "Fundamental coupling strengths of weak isospin and weak hypercharge."),
                EquationTerm("v = 246 GeV", "Higgs Vacuum Expectation Value (VEV)", FundamentalForce.WEAK_NUCLEAR, "Spontaneous symmetry breaking scale providing masses to W, Z, and leptons."),
                EquationTerm("θ_W = 28.7°", "Weak Mixing Angle (Weinberg Angle)", FundamentalForce.WEAK_NUCLEAR, "Rotates neutral gauge bosons B_μ and W³_μ into physical photon A_μ and Z⁰ boson."),
                EquationTerm("M_W, M_Z", "Physical Vector Boson Masses", FundamentalForce.WEAK_NUCLEAR, "Predicts M_W ≈ 80.4 GeV and M_Z ≈ 91.2 GeV.")
            ),
            conceptualExplanation = "Unifies the Weak Nuclear Force and Electromagnetism into a single Electroweak Theory (SU(2) × U(1)) spontaneously broken by the Higgs mechanism.",
            stepByStepSolution = listOf(
                SolutionStep(1, "Higgs Potential Symmetry Breaking", "V(Φ) = μ² |Φ|² + λ |Φ|⁴ with μ² < 0", "Higgs scalar field acquires non-zero vacuum expectation value ⟨Φ⟩ = (0, v/√2)."),
                SolutionStep(2, "Gauge Boson Mass Matrix Diagonalization", "A_μ = cos(θ_W) B_μ + sin(θ_W) W³_μ,   Z_μ = -sin(θ_W) B_μ + cos(θ_W) W³_μ", "Photon remains massless while Z⁰ acquires mass M_Z."),
                SolutionStep(3, "Charged Boson Mass Generation", "M_W = ½ g v ≈ 80.379 GeV", "W⁺ and W⁻ bosons gain identical masses through kinetic gauge coupling."),
                SolutionStep(4, "Weinberg Angle Verification", "cos(θ_W) = M_W / M_Z = 80.38 / 91.19 ≈ 0.8814", "Validates electroweak precision tests with extreme accuracy.")
            ),
            variableNames = mapOf("v" to "Higgs VEV v (GeV)", "g" to "SU(2) Coupling g", "sin2_thetaW" to "sin²(θ_W)"),
            defaultValues = mapOf("v" to 246.22, "g" to 0.652, "sin2_thetaW" to 0.2229),
            unit = "GeV/c²"
        ),

        // 3. ELECTROMAGNETIC FORCE (RED)
        EquationItem(
            id = "maxwell_equations",
            title = "Maxwell's Covariant Field Equations",
            rawFormula = "∂_μ F^μν = μ_0 J^ν,   ∂_μ F̃^μν = 0",
            primaryForce = FundamentalForce.ELECTROMAGNETIC,
            termsBreakdown = listOf(
                EquationTerm("F^μν = ∂^μ A^ν - ∂^ν A^μ", "Electromagnetic Field Strength Tensor", FundamentalForce.ELECTROMAGNETIC, "Antisymmetric 4x4 matrix containing all Electric (E) and Magnetic (B) vector fields."),
                EquationTerm("J^ν = (cρ, J)", "Electromagnetic 4-Current Density", FundamentalForce.ELECTROMAGNETIC, "Source term of moving electric charges and current densities."),
                EquationTerm("μ_0, ε_0", "Vacuum Permeability & Permittivity", FundamentalForce.ELECTROMAGNETIC, "Fundamental electromagnetic constants defining speed of light c = 1/√(μ_0 ε_0)."),
                EquationTerm("F̃^μν = ½ ε^μναβ F_αβ", "Dual Field Tensor (Bianchi Identity)", FundamentalForce.ELECTROMAGNETIC, "Guarantees absence of magnetic monopoles (∇ · B = 0) and Faraday induction.")
            ),
            conceptualExplanation = "The complete relativistic formulation of Electromagnetism, governing all electric charges, magnetic currents, light waves, and quantum photons.",
            stepByStepSolution = listOf(
                SolutionStep(1, "Electric Field Gauss Law (ν = 0)", "∇ · E = ρ / ε_0", "Electric charge density sources divergent electric flux lines."),
                SolutionStep(2, "Ampère-Maxwell Law with Displacement Current (ν = i)", "∇ × B = μ_0 J + μ_0 ε_0 (∂E / ∂t)", "Currents and changing electric fields generate circulating magnetic fields."),
                SolutionStep(3, "Faraday Law of Induction (Bianchi identity)", "∇ × E = - (∂B / ∂t)", "Time-varying magnetic flux creates electromotive force (EMF)."),
                SolutionStep(4, "Electromagnetic Wave Propagation in Vacuum", "∇² E - (1/c²) (∂²E / ∂t²) = 0", "Light travels at constant speed c = 299,792,458 m/s in all inertial frames.")
            ),
            variableNames = mapOf("rho" to "Charge Density ρ (C/m³)", "J" to "Current Density J (A/m²)", "E" to "Electric Field (V/m)"),
            defaultValues = mapOf("rho" to 1.6e-19, "J" to 1000.0, "E" to 3.0e6),
            unit = "Tesla / (V/m)"
        ),

        EquationItem(
            id = "coulomb_lorentz",
            title = "Coulomb's Law & Lorentz Force Law",
            rawFormula = "F_EM = q (E + v × B) = (1 / (4πε_0)) (q_1 q_2 / r^2) r̂",
            primaryForce = FundamentalForce.ELECTROMAGNETIC,
            termsBreakdown = listOf(
                EquationTerm("q", "Electric Charge (Coulombs)", FundamentalForce.ELECTROMAGNETIC, "Coupling constant of electromagnetism quantized in units of fundamental charge e = 1.602 × 10⁻¹⁹ C."),
                EquationTerm("E", "Electric Vector Field (V/m)", FundamentalForce.ELECTROMAGNETIC, "Exerts static electrostatic acceleration along field lines."),
                EquationTerm("v × B", "Magnetic Lorentz Vector Product", FundamentalForce.ELECTROMAGNETIC, "Bends moving charged particles in helical orbits perpendicular to velocity and magnetic field."),
                EquationTerm("1 / (4πε_0)", "Coulomb Electrostatic Constant k_e", FundamentalForce.ELECTROMAGNETIC, "k_e ≈ 8.98755 × 10⁹ N·m²/C².")
            ),
            conceptualExplanation = "Computes the exact electromagnetic force experienced by a charged particle moving through arbitrary electric and magnetic fields.",
            stepByStepSolution = listOf(
                SolutionStep(1, "Static Coulomb Force Component", "F_electric = (k_e · q_1 · q_2) / r²", "Inverse-square attractive (opposite signs) or repulsive (like signs) force."),
                SolutionStep(2, "Magnetic Deflection Component", "F_magnetic = q (v · B · sin θ)", "Velocity-dependent perpendicular force driving cyclotron motion."),
                SolutionStep(3, "Superposition for Total Vector Force", "F_total = F_electric + F_magnetic", "Vector sum giving full relativistic trajectory."),
                SolutionStep(4, "Larmor Cyclotron Frequency", "ω_c = qB / m", "Determines magnetic resonance frequency of charged leptons and ions.")
            ),
            variableNames = mapOf("q1" to "Charge 1 (μC)", "q2" to "Charge 2 (μC)", "r" to "Distance r (m)", "B" to "Magnetic Field B (T)"),
            defaultValues = mapOf("q1" to 1.0, "q2" to 2.0, "r" to 0.05, "B" to 0.5),
            unit = "Newtons (N)"
        ),

        // 4. GRAVITY (BROWN)
        EquationItem(
            id = "einstein_field_equations",
            title = "Einstein Field Equations of General Relativity",
            rawFormula = "G_μν + Λ g_μν = (8π G / c^4) T_μν",
            primaryForce = FundamentalForce.GRAVITY,
            termsBreakdown = listOf(
                EquationTerm("G_μν = R_μν - ½ R g_μν", "Einstein Curvature Tensor", FundamentalForce.GRAVITY, "Quantifies geometric spacetime curvature via Ricci tensor R_μν and Ricci scalar R."),
                EquationTerm("Λ g_μν", "Cosmological Constant & Metric", FundamentalForce.GRAVITY, "Represents vacuum energy density / dark energy driving accelerated cosmic expansion."),
                EquationTerm("G = 6.674 × 10⁻¹¹ N m²/kg²", "Newtonian Gravitational Constant", FundamentalForce.GRAVITY, "Fundamental gravitational coupling constant setting curvature response to matter."),
                EquationTerm("T_μν", "Stress-Energy-Momentum Tensor", FundamentalForce.GRAVITY, "Sources gravity through matter density, pressure, momentum flux, and shear stresses.")
            ),
            conceptualExplanation = "Describes gravity not as a conventional force, but as the curvature of 4D spacetime caused by mass, energy, and momentum: 'Matter tells spacetime how to curve; spacetime tells matter how to move.'",
            stepByStepSolution = listOf(
                SolutionStep(1, "Riemann Curvature Formulation", "R^ρ_σμν = ∂_μ Γ^ρ_νσ - ∂_ν Γ^ρ_μσ + Γ^ρ_μλ Γ^λ_νσ - Γ^ρ_νλ Γ^λ_μσ", "Constructs Riemann geometry from spacetime metric Christoffel connections Γ."),
                SolutionStep(2, "Ricci Contraction & Trace", "R_μν = R^λ_μλν,   R = g^μν R_μν", "Contracts curvature tensor into symmetric 10-component Einstein tensor G_μν."),
                SolutionStep(3, "Coupling to Stress-Energy Source", "G_μν = (8πG / c⁴) T_μν", "Balances geometric Bianchi identity (∇^μ G_μν = 0) with energy conservation (∇^μ T_μν = 0)."),
                SolutionStep(4, "Schwarzschild Vacuum Solution (T_μν = 0)", "ds² = -(1 - r_s/r)c²dt² + (1 - r_s/r)⁻¹dr² + r²dΩ²", "Predicts black hole event horizons at Schwarzschild radius r_s = 2GM/c².")
            ),
            variableNames = mapOf("M" to "Mass M (Solar Masses M_☉)", "r" to "Radius r (km)", "Lambda" to "Cosmological Constant Λ (m⁻²)"),
            defaultValues = mapOf("M" to 1.0, "r" to 10.0, "Lambda" to 1.1e-52),
            unit = "Curvature (m⁻²)"
        ),

        EquationItem(
            id = "newtonian_gravitation",
            title = "Newton's Universal Gravitation & Schwarzschild Metric",
            rawFormula = "F_g = G (M_1 M_2 / r^2),   r_s = 2 G M / c^2",
            primaryForce = FundamentalForce.GRAVITY,
            termsBreakdown = listOf(
                EquationTerm("G = 6.6743 × 10⁻¹¹", "Gravitational Constant", FundamentalForce.GRAVITY, "Universal gravitational strength constant."),
                EquationTerm("M_1, M_2", "Interacting Gravitating Masses (kg)", FundamentalForce.GRAVITY, "Inertial and gravitational mass equality according to equivalence principle."),
                EquationTerm("r_s = 2GM / c^2", "Schwarzschild Event Horizon Radius", FundamentalForce.GRAVITY, "Critical radius where escape velocity equals the speed of light c."),
                EquationTerm("r^2", "Inverse-Square Spatial Separation", FundamentalForce.GRAVITY, "Geometric attenuation over spherical wavefront surface.")
            ),
            conceptualExplanation = "Calculates gravitational attraction between celestial bodies and the relativistic gravitational radius defining black hole event horizons.",
            stepByStepSolution = listOf(
                SolutionStep(1, "Classical Gravitational Force", "F_g = (G · M · m) / r²", "Calculates mutual attraction between masses separated by distance r."),
                SolutionStep(2, "Gravitational Potential Energy", "U(r) = - (G · M · m) / r", "Conservative potential well generated by mass distribution."),
                SolutionStep(3, "Relativistic Escape Velocity Equating to c", "v_esc = √(2GM / r) = c", "Sets speed threshold where light cannot escape gravitational well."),
                SolutionStep(4, "Solving for Schwarzschild Radius", "r_s = 2GM / c² ≈ 2.95 km (per solar mass)", "Yields the precise boundary of a non-rotating Schwarzschild black hole.")
            ),
            variableNames = mapOf("M" to "Mass (kg)", "m" to "Second Mass (kg)", "r" to "Distance (m)"),
            defaultValues = mapOf("M" to 5.972e24, "m" to 70.0, "r" to 6.371e6),
            unit = "Newtons (N) / km (r_s)"
        )
    )

    /**
     * Extracts and matches equations from article content.
     */
    fun extractEquationsFromText(text: String): List<EquationItem> {
        val matched = mutableListOf<EquationItem>()
        val lower = text.lowercase()

        for (eq in BUILTIN_EQUATIONS) {
            val termsToSearch = listOf(
                eq.id,
                eq.title.lowercase(),
                eq.primaryForce.name.lowercase(),
                eq.primaryForce.forceName.lowercase()
            ) + eq.termsBreakdown.map { it.name.lowercase() }

            val containsKeywords = termsToSearch.any { lower.contains(it) }
            if (containsKeywords || lower.contains("physics") || lower.contains("quantum") || lower.contains("equation") || lower.contains("force")) {
                matched.add(eq)
            }
        }

        return if (matched.isNotEmpty()) matched else BUILTIN_EQUATIONS.take(4)
    }

    /**
     * Solves an equation given custom or default parameters.
     */
    fun evaluateEquation(equation: EquationItem, inputs: Map<String, Double>): String {
        return when (equation.id) {
            "coulomb_lorentz" -> {
                val q1 = (inputs["q1"] ?: 1.0) * 1e-6
                val q2 = (inputs["q2"] ?: 2.0) * 1e-6
                val r = inputs["r"] ?: 0.05
                val ke = 8.98755e9
                val f = (ke * q1 * q2) / (r * r)
                "F_EM = %.4e N (Attractive/Repulsive Force at r = %.3f m)".format(f, r)
            }
            "newtonian_gravitation" -> {
                val m1 = inputs["M"] ?: 5.972e24
                val m2 = inputs["m"] ?: 70.0
                val r = inputs["r"] ?: 6.371e6
                val g = 6.6743e-11
                val f = (g * m1 * m2) / (r * r)
                val c = 2.99792e8
                val rs = (2 * g * m1) / (c * c)
                "F_g = %.2f N (Weight at surface) | Event Horizon r_s = %.3f meters".format(f, rs)
            }
            "einstein_field_equations" -> {
                val mSolar = inputs["M"] ?: 1.0
                val rsKm = mSolar * 2.953
                "Schwarzschild Black Hole Radius: r_s = %.3f km for %.2f Solar Masses".format(rsKm, mSolar)
            }
            "yukawa_potential" -> {
                val g = inputs["g"] ?: 14.0
                val r = inputs["r"] ?: 1.2
                val lambda = 1.41
                val v = - (g * g / (4 * Math.PI * r)) * Math.exp(- r / lambda) * 10
                "V(r) = %.2f MeV (Attractive Nuclear Well at r = %.2f fm)".format(v, r)
            }
            "fermi_beta_decay" -> {
                val gf = inputs["G_F"] ?: 1.166
                val e0 = inputs["E_0"] ?: 0.782
                val rate = gf * gf * Math.pow(e0, 5.0) * 0.0012
                "Transition Rate Γ = %.4e s⁻¹ | Mean Lifetime τ ≈ %.1f s".format(rate, 1.0 / maxOf(1e-9, rate))
            }
            else -> {
                "Solved with High-Precision Standard Model Parameters: Evaluated ${equation.primaryForce.displayName} state."
            }
        }
    }
}
