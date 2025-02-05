package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf

import no.nav.brukerdialog.common.Constants.DATE_FORMATTER
import no.nav.brukerdialog.common.Constants.DATE_TIME_FORMATTER
import no.nav.brukerdialog.common.Constants.OSLO_ZONE_ID
import no.nav.brukerdialog.common.FeltMap
import no.nav.brukerdialog.common.PdfConfig
import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.PSBMottattSøknad
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Arbeidsgiver
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.BarnRelasjon
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Frilans
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.StønadGodtgjørelse
import no.nav.brukerdialog.utils.DateUtils.somNorskDag
import java.time.LocalDate

object PSBSøknadPdfDataMapper {
    fun mapPSBSøknadPdfData(
        ytelseTittel: String,
        søknad: PSBMottattSøknad,
    ): FeltMap {
        val innsendingsdetaljer =
            mapInnsendingsdetaljer(
                søknad.mottatt
                    .withZoneSameInstant(OSLO_ZONE_ID)
                    .somNorskDag() + DATE_TIME_FORMATTER.format(søknad.mottatt),
            )

        val søker =
            mapSøker(
                søknad.søker.formatertNavn(),
                søknad.søker.fødselsnummer,
                søknad.barn.navn,
                søknad.barn.fødselsnummer.toString(),
            )

        val relasjonTilBarnet = mapRelasjonTilBarnet(søknad.barnRelasjon, søknad.barnRelasjonBeskrivelse)
        val perioder = mapPerioder(søknad.fraOgMed, søknad.tilOgMed)
        val arbeidsgivere = mapArbeidsgivere(søknad.arbeidsgivere, søknad.fraOgMed)
        val stønadGodtgjørelse = mapStønadGodtgjørelse(søknad.stønadGodtgjørelse)
//        frilans,
        val frilans = mapFrilans(søknad.frilans)

//        jobbISøknadsperioden,
//        opptjeningIUtlandet,
//        utenlandskNæring,
//        omsorgstilbud,
//        verneplikt,
//        nattevåk,
//        beredskap,
//        omsorgsstønad,

//        selvstending,
//        medlemskap,
//        vedlegg,
//        samtykke,
        //        utenlandsopphold,

        return FeltMap(
            label = ytelseTittel,
            verdiliste =
                listOf(
                    innsendingsdetaljer,
                    søker,
                ),
            pdfConfig = PdfConfig(true, "nb"),
        )
    }

    fun mapInnsendingsdetaljer(
        tidspunkt: String,
        språk: String? = "nb",
    ): VerdilisteElement =
        VerdilisteElement(
            label = "Innsendingsdetaljer",
            verdiliste =
                listOf(
                    VerdilisteElement(
                        label = "Sendt til Nav",
                        verdi = tidspunkt,
                    ),
                ),
        )

    fun mapSøker(
        navnSøker: String,
        fødselsnummerSøker: String,
        navnBarn: String,
        fødselsnummerBarn: String,
    ) = VerdilisteElement(
        label = "Søker",
        verdiliste =
            listOf(
                VerdilisteElement(label = "Navn", verdi = navnSøker),
                VerdilisteElement(label = "Fødselsnummer", verdi = fødselsnummerSøker),
                VerdilisteElement(label = "Navn på barn", verdi = navnBarn),
                VerdilisteElement(label = "Fødselsnummer på barn", verdi = fødselsnummerBarn),
            ),
    )

    private fun mapRelasjonTilBarnet(
        barnRelasjon: BarnRelasjon?,
        barnRelasjonBeskrivelse: String?,
    ): VerdilisteElement? =
        barnRelasjon?.let {
            VerdilisteElement(
                label = "Relasjon til barnet",
                verdiliste =
                    listOfNotNull(
                        VerdilisteElement(
                            label = "Hvilken relasjon har du til barnet?",
                            verdi = barnRelasjon.toString(),
                        ),
                        barnRelasjonBeskrivelse?.takeIf { it.isNotBlank() }?.let {
                            VerdilisteElement(
                                label = "Din beskrivelse av relasjon og tilsynsrolle for barnet:",
                                verdi = it,
                            )
                        },
                    ),
            )
        }

    private fun mapPerioder(
        fraOgMed: LocalDate,
        tilOgMed: LocalDate,
    ): VerdilisteElement {
        val fraDato = DATE_FORMATTER.format(fraOgMed)
        val tilDato = DATE_FORMATTER.format(tilOgMed)

        return VerdilisteElement(label = "Perioder du søker om pleiepenger", verdi = "$fraDato - $tilDato")
    }

    private fun mapArbeidsgivere(
        arbeidsgivere: List<Arbeidsgiver>,
        fraOgMed: LocalDate,
    ): VerdilisteElement? =
        if (arbeidsgivere.isNotEmpty()) {
            VerdilisteElement(
                label = "Arbeidsgivere",
                verdiliste =
                    arbeidsgivere.map { arbeidsgiver ->
                        VerdilisteElement(
                            label = arbeidsgiver.navn.toString(),
                            verdiliste =
                                listOfNotNull(
                                    VerdilisteElement(label = "Orgnr:", verdi = arbeidsgiver.organisasjonsnummer),
                                    VerdilisteElement(
                                        label = "Jobber du her nå?",
                                        verdi = konverterBooleanTilSvar(arbeidsgiver.erAnsatt),
                                    ),
                                    arbeidsgiver.sluttetFørSøknadsperiode?.takeIf { it }?.let {
                                        VerdilisteElement(
                                            label = "Sluttet du hos ${arbeidsgiver.navn} før $fraOgMed?",
                                            verdi = konverterBooleanTilSvar(arbeidsgiver.sluttetFørSøknadsperiode),
                                        )
                                    },
                                    VerdilisteElement(
                                        label = "Hvor mange timer jobber du normalt per uke?",
                                        verdi =
                                            arbeidsgiver.arbeidsforhold
                                                ?.normalarbeidstid
                                                ?.timerPerUkeISnitt
                                                .toString(),
                                    ),
                                ),
                        )
                    },
            )
        } else {
            null
        }

    fun mapStønadGodtgjørelse(stønadGodtgjørelse: StønadGodtgjørelse?): VerdilisteElement? =
        stønadGodtgjørelse?.takeIf { it.mottarStønadGodtgjørelse == true }?.let {
            VerdilisteElement(
                label = "Omsorgsstønad eller fosterhjemsgodtgjørelse",
                verdiliste =
                    listOf(
                        VerdilisteElement(
                            label = "Mottar du omsorgsstønad eller fosterhjemsgodtgjørelse?",
                            verdi = konverterBooleanTilSvar(stønadGodtgjørelse.mottarStønadGodtgjørelse!!),
                        ),
                        VerdilisteElement(
                            label = "Startet du å motta dette underveis i perioden du søker for?",
                            verdi =
                                stønadGodtgjørelse.startdato?.let { "${konverterBooleanTilSvar(true)} Startet $it" }
                                    ?: konverterBooleanTilSvar(false),
                        ),
                        VerdilisteElement(
                            label = "Slutter du å motta dette underveis i perioden du søker for?",
                            verdi =
                                stønadGodtgjørelse.sluttdato?.let { "${konverterBooleanTilSvar(true)} Sluttet $it" }
                                    ?: konverterBooleanTilSvar(false),
                        ),
                    ),
            )
        }

    fun mapFrilans(frilans: Frilans?): VerdilisteElement? {
        val sisteTreMånederFørSøknadsperiodeStart = DATE_FORMATTER.format(frilans?.startdato?.minusMonths(3))
        return frilans?.takeIf { it.harInntektSomFrilanser }?.let {
            VerdilisteElement(
                label = "Frilans",
                verdiliste =
                    listOfNotNull(
                        VerdilisteElement(
                            label = "Jobber du som frilanser eller mottar du honorarer?",
                            verdi = konverterBooleanTilSvar(frilans.harInntektSomFrilanser),
                        ),
                        VerdilisteElement(
                            label = "Jobber du som frilanser?",
                            verdi = konverterBooleanTilSvar(frilans.jobberFortsattSomFrilans == true),
                        ),
                        if (frilans.startetFørSisteTreHeleMåneder == true) {
                            VerdilisteElement(
                                label = "Startet du som frilanser før $sisteTreMånederFørSøknadsperiodeStart?",
                                verdi = konverterBooleanTilSvar(true),
                            )
                        } else {
                            VerdilisteElement(
                                label = "Når startet du som frilanser?",
                                verdi = DATE_FORMATTER.format(frilans.startdato),
                            )
                        },
                        VerdilisteElement(
                            label = "Jobber du fremdeles som frilanser?",
                            verdi = konverterBooleanTilSvar(frilans.jobberFortsattSomFrilans == true),
                        ),
                        frilans.jobberFortsattSomFrilans.takeIf { it == false }?.let {
                            VerdilisteElement(
                                label = "Når sluttet du som frilanser?",
                                verdi = DATE_FORMATTER.format(frilans.sluttdato),
                            )
                        },
                        VerdilisteElement(
                            label = "Jeg jobber som frilanser og mottar honorar",
                            verdiliste =
                                listOfNotNull(
                                    if (frilans.startetFørSisteTreHeleMåneder == true) {
                                        VerdilisteElement(
                                            label =
                                                "Startet du som frilanser/startet å motta honorar " +
                                                    "før $sisteTreMånederFørSøknadsperiodeStart?",
                                            verdi = konverterBooleanTilSvar(frilans.startetFørSisteTreHeleMåneder),
                                        )
                                    } else {
                                        VerdilisteElement(
                                            label = "Når begynte du å jobbe som frilanser/startet å motta honorar?",
                                            verdi = DATE_FORMATTER.format(frilans.startdato),
                                        )
                                    },
                                    VerdilisteElement(
                                        label = "Jobber du fremdeles som frilanser/mottar honorar?",
                                        verdi = konverterBooleanTilSvar(frilans.jobberFortsattSomFrilans == true),
                                    ),
                                    frilans.jobberFortsattSomFrilans.takeIf { it == false }?.let {
                                        VerdilisteElement(
                                            label = "Når sluttet du som frilanser/sluttet å motta honorar?",
                                            verdi = DATE_FORMATTER.format(frilans.sluttdato),
                                        )
                                    },
                                ),
                        ),
                        VerdilisteElement(
                            label = "Jeg mottar honorar",
                            verdiliste =
                                listOfNotNull(
                                    if (frilans.startetFørSisteTreHeleMåneder == true) {
                                        VerdilisteElement(
                                            label =
                                                "Startet du som frilanser/startet å motta honorar " +
                                                    "før $sisteTreMånederFørSøknadsperiodeStart?",
                                            verdi = konverterBooleanTilSvar(frilans.startetFørSisteTreHeleMåneder),
                                        )
                                    } else {
                                        VerdilisteElement(
                                            label = "Når begynte du å motta honorar?",
                                            verdi = DATE_FORMATTER.format(frilans.startdato),
                                        )
                                    },
                                    VerdilisteElement(
                                        label = "Mottar du fortsatt honorarer?",
                                        verdi = konverterBooleanTilSvar(frilans.jobberFortsattSomFrilans == true),
                                    ),
                                    frilans.jobberFortsattSomFrilans.takeIf { it == false }?.let {
                                        VerdilisteElement(
                                            label = "Når sluttet du å motta honorar?",
                                            verdi = DATE_FORMATTER.format(frilans.sluttdato),
                                        )
                                    },
                                    if (frilans.misterHonorar == true) {
                                        VerdilisteElement(
                                            label = "Jeg mister honorar i søknadsperioden",
                                        )
                                    } else {
                                        VerdilisteElement(
                                            label = "Jeg mister ikke honorar i søknadsperioden",
                                        )
                                    },
                                ),
                        ),
                        VerdilisteElement(
                            label = "Hvor mange timer jobber du normalt per uke?",
                            verdi =
                                frilans.arbeidsforhold
                                    ?.normalarbeidstid
                                    ?.timerPerUkeISnitt
                                    .toString(),
                        ),
                    ),
            )
        } ?: VerdilisteElement(
            label = "Frilans",
            verdi = "Har ikke vært frilanser eller mottatt honorar i perioden det søkes om.",
        )
    }

    fun konverterBooleanTilSvar(svar: Boolean) =
        if (svar) {
            "Ja"
        } else {
            "Nei"
        }
}
