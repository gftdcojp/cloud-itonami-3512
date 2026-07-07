(ns energy.facts
  "Per-jurisdiction grid-interconnection/tariff-policy regulatory
  catalog -- the G2-style spec-basis table the Grid Policy Governor
  checks every tariff/verify proposal against ('did the advisor cite
  an OFFICIAL public source for this jurisdiction's grid-
  interconnection and tariff requirements, or did it invent one?').

  Coverage is reported HONESTLY (see `coverage`), the same discipline
  every sibling actor's `facts` namespace uses: a jurisdiction not in
  this table has NO spec-basis, full stop -- the advisor must not
  fabricate one, and the governor holds if it tries.

  Seed values are drawn from each jurisdiction's official grid-
  interconnection/tariff regulator (see `:provenance`); they are a
  STARTING catalog, not a from-scratch survey of all ~194
  jurisdictions. Extending coverage is additive: add one map to
  `catalog`, cite a real source, done -- never invent a jurisdiction's
  requirements to make coverage look bigger.")

(def catalog
  "iso3 -> requirement map. `:required-evidence` mirrors the generic
  site-telemetry-provenance-record/interconnection-agreement-record/
  tariff-schedule-record/settlement-source-data-record evidence set
  submitted in some form; `:legal-basis` / `:owner-authority` /
  `:provenance` are the G2 citation the governor requires before any
  `:tariff/verify` proposal can commit."
  {"JPN" {:name "Japan"
          :owner-authority "経済産業省資源エネルギー庁 (ANRE, Agency for Natural Resources and Energy)"
          :legal-basis "電気事業法 (Electricity Business Act) / 系統連系規程"
          :national-spec "分散型電源の系統連系および料金・精算に関する要件"
          :provenance "https://www.enecho.meti.go.jp/category/electricity_and_gas/electric/"
          :required-evidence ["設備遠隔計測出所記録 (site-telemetry-provenance-record)"
                              "系統連系契約記録 (interconnection-agreement-record)"
                              "料金表記録 (tariff-schedule-record)"
                              "精算元データ記録 (settlement-source-data-record)"]}
   "USA" {:name "United States"
          :owner-authority "Federal Energy Regulatory Commission (FERC) / North American Electric Reliability Corporation (NERC)"
          :legal-basis "Federal Power Act / NERC Reliability Standards"
          :national-spec "Distributed-energy-resource interconnection and settlement requirements"
          :provenance "https://www.ferc.gov/electric-transmission"
          :required-evidence ["Site-telemetry-provenance record"
                              "Interconnection-agreement record"
                              "Tariff-schedule record"
                              "Settlement-source-data record"]}
   "GBR" {:name "United Kingdom"
          :owner-authority "Office of Gas and Electricity Markets (Ofgem)"
          :legal-basis "Distribution Connection and Use of System Agreement (DCUSA) / Grid Code"
          :national-spec "Distributed-generation connection and settlement requirements"
          :provenance "https://www.ofgem.gov.uk/energy-policy-and-regulation/policy-and-regulatory-programmes/connections"
          :required-evidence ["Site-telemetry-provenance record"
                              "Interconnection-agreement record"
                              "Tariff-schedule record"
                              "Settlement-source-data record"]}
   "DEU" {:name "Germany"
          :owner-authority "Bundesnetzagentur"
          :legal-basis "Energiewirtschaftsgesetz (EnWG) / VDE-AR-N technische Anschlussregeln"
          :national-spec "Netzanschluss- und Abrechnungsanforderungen für dezentrale Erzeugungsanlagen"
          :provenance "https://www.bundesnetzagentur.de/DE/Fachthemen/ElektrizitaetundGas/Netzanschluss/start.html"
          :required-evidence ["Anlagenfernmessnachweis (site-telemetry-provenance-record)"
                              "Netzanschlussvertrag (interconnection-agreement-record)"
                              "Tarifblattaufzeichnung (tariff-schedule-record)"
                              "Abrechnungsquelldatennachweis (settlement-source-data-record)"]}})

(defn spec-basis
  "The jurisdiction's requirement map, or nil -- nil means NO spec-basis,
  and the governor must hold any proposal that tries to dispatch a
  battery action or finalize a settlement on it."
  [iso3]
  (get catalog iso3))

(defn coverage
  "Honest coverage report: how many of the requested jurisdictions actually
  have a spec-basis entry. Never report a missing jurisdiction as covered."
  ([] (coverage (keys catalog)))
  ([iso3s]
   (let [have (filter catalog iso3s)
         missing (remove catalog iso3s)]
     {:requested (count iso3s)
      :covered (count have)
      :covered-jurisdictions (vec (sort have))
      :missing-jurisdictions (vec (sort missing))
      :note (str "cloud-itonami-isic-3512 R0: " (count catalog)
                 " jurisdictions seeded with an official spec-basis. "
                 "This is a starting catalog, not a survey of all ~194 "
                 "jurisdictions -- extend `energy.facts/catalog`, "
                 "never fabricate a jurisdiction's requirements.")})))

(defn required-evidence-satisfied?
  "Does `submitted` (a set/coll of evidence keywords or strings) satisfy
  every evidence item listed for `iso3`? Missing spec-basis -> never
  satisfied."
  [iso3 submitted]
  (when-let [{:keys [required-evidence]} (spec-basis iso3)]
    (let [need (count required-evidence)
          have (count (filter (set submitted) required-evidence))]
      (= need have))))

(defn evidence-checklist [iso3]
  (:required-evidence (spec-basis iso3) []))
