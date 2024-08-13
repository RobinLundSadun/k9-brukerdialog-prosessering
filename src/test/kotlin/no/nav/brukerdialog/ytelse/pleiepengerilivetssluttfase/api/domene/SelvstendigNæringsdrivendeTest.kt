package no.nav.brukerdialog.ytelse.pleiepengerilivetssluttfase.api.domene

import no.nav.k9.søknad.JsonUtils
import no.nav.brukerdialog.ytelse.fellesdomene.Næringstype.DAGMAMMA
import no.nav.brukerdialog.ytelse.fellesdomene.Regnskapsfører
import no.nav.brukerdialog.ytelse.fellesdomene.VarigEndring
import no.nav.brukerdialog.ytelse.fellesdomene.Virksomhet
import no.nav.brukerdialog.ytelse.fellesdomene.YrkesaktivSisteTreFerdigliknedeArene
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.api.domene.JobberIPeriodeSvar.HELT_FRAVÆR
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.api.domene.JobberIPeriodeSvar.REDUSERT
import no.nav.brukerdialog.ytelse.pleiepengerilivetssluttfase.api.domene.PILSTestUtils.enkeltDagerMedFulltFravær
import no.nav.brukerdialog.ytelse.pleiepengerilivetssluttfase.api.domene.PILSTestUtils.fredag
import no.nav.brukerdialog.ytelse.pleiepengerilivetssluttfase.api.domene.PILSTestUtils.mandag
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.api.domene.ArbeidIPeriode
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.api.domene.Arbeidsforhold
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.api.domene.SelvstendigNæringsdrivende
import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserIngenValideringsFeil
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate

class SelvstendigNæringsdrivendeTest {

    @Test
    fun `Mapping til k9Format blir som forventet`() {
        val k9Virksomhet = SelvstendigNæringsdrivende(
            virksomhet = Virksomhet(
                fraOgMed = LocalDate.parse("2022-01-01"),
                tilOgMed = LocalDate.parse("2022-10-01"),
                næringstype = DAGMAMMA,
                næringsinntekt = 3_000_000,
                navnPåVirksomheten = "Kiwi ASA",
                organisasjonsnummer = "975959171",
                registrertINorge = true,
                yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeArene(
                    oppstartsdato = LocalDate.parse("2022-01-01")
                ),
                varigEndring = VarigEndring(
                    dato = LocalDate.parse("2022-01-01"),
                    inntektEtterEndring = 1_500_00,
                    forklaring = "Fordi atte atte atte"
                ),
                regnskapsfører = Regnskapsfører(
                    navn = "Knut",
                    telefon = "123123123"
                ),
                erNyoppstartet = true,
                harFlereAktiveVirksomheter = true
            ),
            arbeidsforhold = Arbeidsforhold(37.5, ArbeidIPeriode(HELT_FRAVÆR, emptyList()))
        ).somK9SelvstendigNæringsdrivende()

        val forventet = """
            {
              "perioder": {
                "2022-01-01/2022-10-01": {
                  "virksomhetstyper": [
                    "DAGMAMMA"
                  ],
                  "regnskapsførerNavn": "Knut",
                  "regnskapsførerTlf": "123123123",
                  "erVarigEndring": true,
                  "erNyIArbeidslivet": true,
                  "endringDato": "2022-01-01",
                  "endringBegrunnelse": "Fordi atte atte atte",
                  "bruttoInntekt": 150000,
                  "erNyoppstartet": true,
                  "registrertIUtlandet": false,
                  "landkode": "NOR"
                }
              },
              "organisasjonsnummer": "975959171",
              "virksomhetNavn": "Kiwi ASA"
            }
        """.trimIndent()

        JSONAssert.assertEquals(forventet, JsonUtils.toString(k9Virksomhet), true)
    }

    @Test
    fun `Mapping til K9Arbeidstid blir som forventet`() {
        val mandag = mandag
        val fredag = fredag
        val arbeidstidInfo = SelvstendigNæringsdrivende(
            virksomhet = Virksomhet(
                fraOgMed = LocalDate.parse("2022-01-01"),
                tilOgMed = LocalDate.parse("2022-10-01"),
                næringstype = DAGMAMMA,
                navnPåVirksomheten = "Kiwi ASA",
                erNyoppstartet = true,
                registrertINorge = true,
            ),
            arbeidsforhold = Arbeidsforhold(37.5, ArbeidIPeriode(HELT_FRAVÆR, enkeltDagerMedFulltFravær))
        ).somK9ArbeidstidInfo(mandag, fredag)
        // language=json
        val forventet = """
            {
              "perioder": {
                "2022-08-01/2022-08-01": {
                  "jobberNormaltTimerPerDag": "PT7H30M",
                  "faktiskArbeidTimerPerDag": "PT0S"
                },
                "2022-08-02/2022-08-02": {
                  "jobberNormaltTimerPerDag": "PT7H30M",
                  "faktiskArbeidTimerPerDag": "PT0S"
                },
                "2022-08-03/2022-08-03": {
                  "jobberNormaltTimerPerDag": "PT7H30M",
                  "faktiskArbeidTimerPerDag": "PT0S"
                },
                "2022-08-04/2022-08-04": {
                  "jobberNormaltTimerPerDag": "PT7H30M",
                  "faktiskArbeidTimerPerDag": "PT0S"
                },
                "2022-08-05/2022-08-05": {
                  "jobberNormaltTimerPerDag": "PT7H30M",
                  "faktiskArbeidTimerPerDag": "PT0S"
                }
              }
            }
        """.trimIndent()

        JSONAssert.assertEquals(forventet, JsonUtils.toString(arbeidstidInfo), true)
    }

    @Test
    fun `Gyldig SelvstendigNæringsdrivende gir ingen valideringsfeil`() {
        Validator.verifiserIngenValideringsFeil(
            SelvstendigNæringsdrivende(
                virksomhet = Virksomhet(
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2022-10-01"),
                    næringstype = DAGMAMMA,
                    næringsinntekt = 3_000_000,
                    navnPåVirksomheten = "Kiwi ASA",
                    organisasjonsnummer = "975959171",
                    registrertINorge = true,
                    yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeArene(
                        oppstartsdato = LocalDate.parse("2022-01-01")
                    ),
                    varigEndring = VarigEndring(
                        dato = LocalDate.parse("2022-01-01"),
                        inntektEtterEndring = 1_500_00,
                        forklaring = "Fordi atte atte atte"
                    ),
                    regnskapsfører = Regnskapsfører(
                        navn = "Knut",
                        telefon = "123123123"
                    ),
                    erNyoppstartet = true,
                    harFlereAktiveVirksomheter = true
                ),
                arbeidsforhold = Arbeidsforhold(37.5, ArbeidIPeriode(HELT_FRAVÆR, enkeltDagerMedFulltFravær))
            )
        )
    }

    @Test
    fun `SelvstendigNæringsdrivende med feil i virksomhet og arbeidsforhold skal gi valideringsfeil`() {
        Validator.verifiserValideringsFeil(
            SelvstendigNæringsdrivende(
                virksomhet = Virksomhet(
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2022-10-01"),
                    næringstype = DAGMAMMA,
                    næringsinntekt = 3_000_000,
                    navnPåVirksomheten = "Kiwi ASA",
                    organisasjonsnummer = "123ABC",
                    registrertINorge = true,
                    regnskapsfører = Regnskapsfører(
                        navn = "Knut",
                        telefon = "123123123"
                    ),
                    erNyoppstartet = true,
                    harFlereAktiveVirksomheter = true
                ),
                arbeidsforhold = Arbeidsforhold(37.5, ArbeidIPeriode(REDUSERT, emptyList()))
            ),
            2,
            "'123ABC' matcher ikke tillatt pattern '^\\d+$'",
            "Kan ikke være tom liste"
        )
    }
}
