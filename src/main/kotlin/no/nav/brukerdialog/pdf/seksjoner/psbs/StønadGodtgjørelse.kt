package no.nav.brukerdialog.pdf.seksjoner.psbs

import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.StønadGodtgjørelse
import no.nav.brukerdialog.pdf.SpørsmålOgSvar
import no.nav.brukerdialog.pdf.lagVerdiElement
import no.nav.brukerdialog.pdf.tilSpørsmålOgSvar

data class StønadGodtgjørelseSpørsmålOgSvar(
    val mottarStønadGodtgjørelse: SpørsmålOgSvar? = null,
    val startetÅMottaUnderveisTekst: SpørsmålOgSvar? = null,
    val sluttetÅMottaUnderveisTekst: SpørsmålOgSvar? = null,
)

fun strukturerStønadGodtgjørelseSeksjon(søknadSvarStønadGodtgjørelse: StønadGodtgjørelse?): VerdilisteElement {
    val stønadGodtgjørelse = mapStønadGodtgjørelseTilSpørsmålOgSvar(søknadSvarStønadGodtgjørelse)
    return VerdilisteElement(
        label = "Omsorgsstønad eller fosterhjemsgodtgjørelse",
        verdiliste =
            listOfNotNull(
                lagVerdiElement(stønadGodtgjørelse.mottarStønadGodtgjørelse),
                lagVerdiElement(stønadGodtgjørelse.startetÅMottaUnderveisTekst),
                lagVerdiElement(stønadGodtgjørelse.sluttetÅMottaUnderveisTekst),
            ),
    )
}

fun mapStønadGodtgjørelseTilSpørsmålOgSvar(stønadGodtgjørelse: StønadGodtgjørelse?): StønadGodtgjørelseSpørsmålOgSvar =
    StønadGodtgjørelseSpørsmålOgSvar(
        mottarStønadGodtgjørelse =
            stønadGodtgjørelse
                ?.mottarStønadGodtgjørelse
                ?.takeIf { it }
                ?.let {
                    tilSpørsmålOgSvar(
                        "Mottar du omsorgsstønad eller fosterhjemsgodtgjørelse?",
                        stønadGodtgjørelse.mottarStønadGodtgjørelse,
                    )
                },
        startetÅMottaUnderveisTekst =
            tilSpørsmålOgSvar(
                "Startet du å motta dette underveis i perioden du søker for?",
                stønadGodtgjørelse?.startdato?.let { "Ja. Startet $it" } ?: "Nei",
            ),
        sluttetÅMottaUnderveisTekst =
            tilSpørsmålOgSvar(
                "Slutter du å motta dette underveis i perioden du søker for?",
                stønadGodtgjørelse?.sluttdato?.let { "Ja. Sluttet $it" } ?: "Nei",
            ),
    )
