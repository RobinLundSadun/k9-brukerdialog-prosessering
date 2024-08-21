package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene

import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserIngenValideringsFeil
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import org.junit.jupiter.api.Test
import java.time.LocalDate

class BarnValidationTest {
    private companion object {
        val felt = "barn"
        val gyldigBarn = BarnDetaljer(
            fødselsnummer = "02119970078",
            fødselsdato = LocalDate.parse("2021-01-01"),
            aktørId = "10000001",
            navn = "Barnesen",
            årsakManglerIdentitetsnummer = null
        )
    }

    @Test
    fun `Gyldig barn gir ingen feil`() {
        Validator.verifiserIngenValideringsFeil(gyldigBarn)
    }

    @Test
    fun `Skal ikke gi feil selvom fødselsnummer er null så lenge fødselsdato og årsak er satt`() {
        Validator.verifiserIngenValideringsFeil(
            gyldigBarn.copy(
                fødselsnummer = null,
                fødselsdato = LocalDate.parse("2021-01-01"),
                årsakManglerIdentitetsnummer = ÅrsakManglerIdentitetsnummer.NYFØDT
            )
        )
    }

    @Test
    fun `Skal gi feil dersom fødselsnummer ikke settes og man ikke har satt fødsesldato og årsak`() {
        val ugyldigBarn = gyldigBarn.copy(
            fødselsnummer = null,
            årsakManglerIdentitetsnummer = null,
            fødselsdato = null
        )
        Validator.verifiserValideringsFeil(
            ugyldigBarn, 1, "Må være satt dersom fødselsnummer er null."
        )

        ugyldigBarn.valider("barn").verifiserValideringsFeil(1, listOf(
            "barn.årsakManglerIdentitetsnummer må være satt dersom fødselsnummer er null."
        ))
    }

    @Test
    fun `Skal gi feil dersom fødselsdato er i fremtiden`() {
        Validator.verifiserValideringsFeil(
            gyldigBarn.copy(
                fødselsdato = LocalDate.now().plusDays(1)
            ), 1, "kan ikke være i fremtiden"
        )
    }

    @Test
    fun `Forvent valideringsfeil dersom norskIdentifikator er ugyldig`() {
        Validator.verifiserValideringsFeil(
            gyldigBarn.copy(fødselsnummer = "123ABC"),
            2,
            "'123ABC' matcher ikke tillatt pattern '^\\d+\$'",
            "size must be between 11 and 11"
        )
    }
}


