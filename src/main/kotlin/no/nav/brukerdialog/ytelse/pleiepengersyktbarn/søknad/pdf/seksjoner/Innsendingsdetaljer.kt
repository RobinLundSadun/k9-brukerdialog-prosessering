package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner

import no.nav.brukerdialog.common.Constants.DATE_TIME_FORMATTER
import no.nav.brukerdialog.common.Constants.OSLO_ZONE_ID
import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.pdf.SpørsmålOgSvar
import no.nav.brukerdialog.pdf.lagVerdiElement
import no.nav.brukerdialog.pdf.tilSpørsmålOgSvar
import no.nav.brukerdialog.utils.DateUtils.somNorskDag
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.PdfTekster
import java.time.ZonedDateTime

data class InnsendingsdetaljerSpørsmålOgSvar(
    val mottattSøknadTidspunkt: SpørsmålOgSvar?,
)

fun strukturerInnsendingsdetaljerSeksjon(søknadSvarInnsendingsdetaljer: ZonedDateTime): VerdilisteElement {
    val innsendingsdetaljer = mapInnsendingsdetaljerTilSpørsmålOgSvar(søknadSvarInnsendingsdetaljer)
    return VerdilisteElement(
        label = PdfTekster.getValue("innsendingsdetaljer.tittel"),
        verdiliste =
            listOfNotNull(
                lagVerdiElement(innsendingsdetaljer.mottattSøknadTidspunkt),
            ),
    )
}

fun mapInnsendingsdetaljerTilSpørsmålOgSvar(mottattSøknadTidspunkt: ZonedDateTime): InnsendingsdetaljerSpørsmålOgSvar {
    val tidspunkt = "${mottattSøknadTidspunkt.withZoneSameInstant(
        OSLO_ZONE_ID,
    ).somNorskDag()} ${DATE_TIME_FORMATTER.format(mottattSøknadTidspunkt)}"

    return InnsendingsdetaljerSpørsmålOgSvar(mottattSøknadTidspunkt = tilSpørsmålOgSvar(spørsmål = "Sendt til Nav", svar = tidspunkt))
}
