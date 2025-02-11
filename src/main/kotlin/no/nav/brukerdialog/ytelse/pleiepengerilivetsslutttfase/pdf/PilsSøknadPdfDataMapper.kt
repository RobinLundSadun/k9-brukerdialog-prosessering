package no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.pdf

import no.nav.brukerdialog.common.Constants.DATE_FORMATTER
import no.nav.brukerdialog.common.Constants.DATE_TIME_FORMATTER
import no.nav.brukerdialog.common.Constants.OSLO_ZONE_ID
import no.nav.brukerdialog.common.FeltMap
import no.nav.brukerdialog.common.PdfConfig
import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.Perioder
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.Arbeidsgiver
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.FlereSokereSvar
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.Frilans
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.PilsSøknadMottatt
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.Pleietrengende
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.capitalizeName
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.grupperSammenhengendeDatoerPerUkeTypet
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Periode
import no.nav.brukerdialog.utils.DateUtils.grupperMedUker
import no.nav.brukerdialog.utils.DateUtils.somNorskDag
import no.nav.brukerdialog.utils.DurationUtils.tilString
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import java.time.Duration
import java.time.LocalDate

object PilsSøknadPdfDataMapper {
    fun mapPilsSøknadPdfData(
        ytelseTittel: String,
        søknad: PilsSøknadMottatt,
    ): FeltMap {
        val periode: Periode =
            Periode(
                søknad.fraOgMed,
                søknad.tilOgMed,
            )
        val innsendingsdetaljer =
            mapInnsendingsdetaljer(
                søknad.mottatt
                    .withZoneSameInstant(OSLO_ZONE_ID)
                    .somNorskDag() + DATE_TIME_FORMATTER.format(søknad.mottatt),
            )
        val søker = mapSøker(søknad.søker)
        val pleietrengende = mapPleietrengende(søknad.pleietrengende, søknad.flereSokere)
        val perioder = mapPerioder(søknad)
        val mapArbeidsgiver = mapArbeidsGivere(søknad.arbeidsgivere, periode)

        return FeltMap(
            label = ytelseTittel,
            verdiliste =
                listOfNotNull(
                    innsendingsdetaljer,
                    søker,
                    pleietrengende,
                    perioder,
                ),
            pdfConfig = PdfConfig(harInnholdsfortegnelse = true, "nb"),
        )
    }

    // TODO FELLES-FUN
    fun lagVerdiElement(
        spørsmålsTekst: String,
        svarVerdi: Any?,
        typeSomSkalSjekkes: Any? = svarVerdi,
    ): VerdilisteElement? =
        if (typeSomSkalSjekkes == null) {
            null
        } else {
            when (svarVerdi) {
                is String -> VerdilisteElement(label = spørsmålsTekst, verdi = svarVerdi)
                is Enum<*> -> VerdilisteElement(label = spørsmålsTekst, verdi = svarVerdi.toString())
                is Boolean -> VerdilisteElement(label = spørsmålsTekst, verdi = konverterBooleanTilSvar(svarVerdi))
                is Duration -> VerdilisteElement(label = spørsmålsTekst, verdi = svarVerdi.tilString())
                is LocalDate -> VerdilisteElement(label = spørsmålsTekst, verdi = DATE_FORMATTER.format(svarVerdi))
                is Int -> VerdilisteElement(label = spørsmålsTekst, verdi = svarVerdi.toString())
                is Double -> VerdilisteElement(label = spørsmålsTekst, verdi = svarVerdi.toString())
                else -> null
            }
        }

    fun mapSøker(søker: Søker): VerdilisteElement =
        VerdilisteElement(
            "Søker",
            verdiliste =
                listOfNotNull(
                    lagVerdiElement("Navn", søker.formatertNavn()),
                    lagVerdiElement("Fødselsnummer", søker.fødselsnummer),
                ),
        )

    fun mapPleietrengende(
        pleietrengende: Pleietrengende,
        flereSokereSvar: FlereSokereSvar?,
    ): VerdilisteElement =
        VerdilisteElement(
            "Om personen du pleier",
            verdiliste =
                listOfNotNull(
                    lagVerdiElement("Navn", pleietrengende.navn),
                    lagVerdiElement("Fødselsdato", pleietrengende.fødselsdato),
                    lagVerdiElement(
                        "Er dere flere som skal dele på pleiepengene?",
                        flereSokereSvar.toString().capitalizeName(),
                        flereSokereSvar,
                    ),
                ),
        )

    fun mapPerioder(søknad: PilsSøknadMottatt): VerdilisteElement {
        val dagerMedPleie: List<LocalDate> = søknad.dagerMedPleie
        val pleierDuDenSykeHjemme: Boolean = søknad.pleierDuDenSykeHjemme
        val utenlandsoppholdIPerioden = søknad.utenlandsoppholdIPerioden
        val formaterteDatoer = formaterDatoer(dagerMedPleie)
        return VerdilisteElement(
            "Dagene du søker pleiepenger for",
            verdiliste =
                listOfNotNull(
                    lagVerdiElement("Antall dager med pleiepenger", dagerMedPleie.size),
                    *formaterteDatoer.uker
                        .map { uke ->
                            VerdilisteElement(
                                label = "Uke: $uke",
                                visningsVariant = "PUNKTLISTE",
                                verdiliste =
                                    uke.perioder.map { periode ->
                                        VerdilisteElement(
                                            label = periode,
                                        )
                                    },
                            )
                        }.toTypedArray(),
                    lagVerdiElement("Skal du pleie personen hjemme i de dagene du søker for?", pleierDuDenSykeHjemme),
                    lagVerdiElement("Skal du jobbe delvis i noen av dagene du søker for?", søknad.skalJobbeOgPleieSammeDag),
                    utenlandsoppholdIPerioden.skalOppholdeSegIUtlandetIPerioden?.let {
                        VerdilisteElement(
                            label = "Oppholder du deg i utlandet i noen av dagene du søker for?",
                            verdi = konverterBooleanTilSvar(utenlandsoppholdIPerioden.skalOppholdeSegIUtlandetIPerioden),
                            verdiliste =
                                utenlandsoppholdIPerioden.skalOppholdeSegIUtlandetIPerioden
                                    .let {
                                        utenlandsoppholdIPerioden.opphold.flatMap { opphold ->
                                            listOfNotNull(
                                                lagVerdiElement(
                                                    "Opphold i ${opphold.landnavn}",
                                                    "${DATE_FORMATTER.format(
                                                        opphold.fraOgMed,
                                                    )} - ${DATE_FORMATTER.format(opphold.tilOgMed)}",
                                                ),
                                            )
                                        }
                                    },
                        )
                    },
                ),
        )
    }

    fun mapArbeidsGivere(
        arbeidsgivere: List<Arbeidsgiver>,
        periode: Periode,
    ) = arbeidsgivere.isNotEmpty().let {
        VerdilisteElement(
            label = "Arbeidsgivere",
            verdiliste =
                arbeidsgivere.flatMap { arbeidsgiver ->
                    listOfNotNull(
                        arbeidsgiver.navn?.let {
                            VerdilisteElement(
                                label = it + "( Orgnr: " + arbeidsgiver.organisasjonsnummer + ")",
                                visningsVariant = "PUNKTLISTE",
                                verdiliste =
                                    listOfNotNull(
                                        if (arbeidsgiver.erAnsatt) {
                                            lagVerdiElement("Er ansatt", true)
                                        } else {
                                            VerdilisteElement(
                                                label = "Er ikke ansatt i perioden",
                                                verdiliste =
                                                    listOfNotNull(
                                                        lagVerdiElement(
                                                            "Sluttet du hos ${arbeidsgiver.navn} før ${periode.fraOgMed}?",
                                                            arbeidsgiver.sluttetFørSøknadsperiode,
                                                            arbeidsgiver.sluttetFørSøknadsperiode,
                                                        ),
                                                    ),
                                            )
                                        },
                                        lagVerdiElement(
                                            "Hvor mange timer jobber du normalt per uke?",
                                            arbeidsgiver.arbeidsforhold?.jobberNormaltTimer,
                                            arbeidsgiver.arbeidsforhold,
                                        ),
                                    ),
                            )
                        },
                    )
                },
        )
    }

    fun mapFrilans(frilans: Frilans?): VerdilisteElement =
        when {
            frilans?.harHattInntektSomFrilanser == true ->
                VerdilisteElement(
                    "Frilans",
                    visningsVariant = "PUNKTLISTE",
                    verdiliste =
                        listOf(
                            VerdilisteElement(
                                label = "Startet som frilanser ${frilans.startdato}",
                                verdiliste =
                                    listOfNotNull(
                                        lagVerdiElement(
                                            "Hvor mange timer jobber du normalt per uke?",
                                            frilans.arbeidsforhold?.jobberNormaltTimer,
                                            frilans.arbeidsforhold,
                                        ),
                                        VerdilisteElement(
                                            label = "Jobber du fortsatt som frilans?",
                                            verdi =
                                                elseIfSvar(
                                                    frilans.jobberFortsattSomFrilans,
                                                    "Ja",
                                                    "Nei, Sluttet som frilanser ${frilans.sluttdato}",
                                                ),
                                        ),
                                        frilans.takeUnless { it.jobberFortsattSomFrilans }?.let {
                                            lagVerdiElement("Når sluttet du som frilanser?", frilans.sluttdato)
                                        },
                                    ),
                            ),
                        ),
                )
            else -> VerdilisteElement(label = "Er ikke frilanser i perioden det søkes om.")
        }

    fun mapSelvstendigNæringsdrivende(): VerdilisteElement =
        VerdilisteElement(
            "Selvstendig næringsdrivende",
            verdiliste =
                listOf(
                    VerdilisteElement(
                        label = "Er ikke selvstendig næringsdrivende i perioden det søkes om.",
                    ),
                ),
        )

    fun formaterDatoer(dagerMedPleie: List<LocalDate>): PleieDager =
        PleieDager(
            dagerMedPleie.size,
            dagerMedPleie.map { DATE_FORMATTER.format(it) },
            dagerMedPleie.grupperMedUker().grupperSammenhengendeDatoerPerUkeTypet(),
        )

    data class PleieDager(
        val totalAntallDagerMedPleie: Int,
        val datoer: List<String>,
        val uker: List<Perioder>,
    )

    fun konverterBooleanTilSvar(svar: Boolean) =
        if (svar) {
            "Ja"
        } else {
            "Nei"
        }

    private fun mapInnsendingsdetaljer(tidspunkt: String): VerdilisteElement =
        VerdilisteElement(
            label = "Innsendingsdetaljer",
            verdiliste =
                listOfNotNull(
                    lagVerdiElement("Send til Nav", tidspunkt),
                ),
        )

    private fun elseIfSvar(
        svar: Boolean,
        svarTrue: String,
        svarFalse: String,
    ): String =
        if (svar) {
            svarTrue
        } else {
            svarFalse
        }
}
