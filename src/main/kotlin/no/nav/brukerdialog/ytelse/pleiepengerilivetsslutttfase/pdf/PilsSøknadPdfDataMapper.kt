package no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.pdf

import no.nav.brukerdialog.common.Constants.DATE_FORMATTER
import no.nav.brukerdialog.common.Constants.DATE_TIME_FORMATTER
import no.nav.brukerdialog.common.Constants.OSLO_ZONE_ID
import no.nav.brukerdialog.common.FeltMap
import no.nav.brukerdialog.common.PdfConfig
import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.Perioder
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.PilsSøknadPdfData
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.Arbeidsforhold
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.Arbeidsgiver
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.Bosted
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.FlereSokereSvar
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.Frilans
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.Medlemskap
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.Næringstype
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.OpptjeningIUtlandet
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.PilsSøknadMottatt
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.Pleietrengende
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.SelvstendigNæringsdrivende
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.UtenlandskNæring
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.capitalizeName
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.grupperSammenhengendeDatoerPerUkeTypet
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.mapArbeidsforholdTilType
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
        val arbeidsgiver = mapArbeidsGivere(søknad.arbeidsgivere, periode)
        val frilans = mapFrilans(søknad.frilans)
        val selvstendigNaeringsdrivende = mapSelvstendigNæringsdrivende(søknad.selvstendigNæringsdrivende)
        val opptjeningIUtlandet = mapOpptjeningIUtlandet(søknad.opptjeningIUtlandet)

        val pilsSøknadPdfData =
            PilsSøknadPdfData(
                søknad,
            )

        return FeltMap(
            label = ytelseTittel,
            verdiliste =
                listOfNotNull(
                    innsendingsdetaljer,
                    søker,
                    pleietrengende,
                    perioder,
                    arbeidsgiver,
                    frilans,
                    selvstendigNaeringsdrivende,
                    opptjeningIUtlandet,
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
                                                    mapPeriodeTilString(opphold.fraOgMed, opphold.tilOgMed),
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

    fun mapSelvstendigNæringsdrivende(selvstendigNæringsdrivende: SelvstendigNæringsdrivende?): VerdilisteElement? =
        selvstendigNæringsdrivende?.run {
            VerdilisteElement(
                "Selvstendig næringsdrivende",
                verdiliste =
                    listOfNotNull(
                        lagVerdiElement("Er du selvstendig næringsdrivende i perioden?", true),
                        lagVerdiElement(
                            "Har du flere virksomheter?",
                            selvstendigNæringsdrivende.virksomhet.harFlereAktiveVirksomheter,
                        ),
                        lagVerdiElement(
                            "Hvor mange timer jobber du normalt per uke?",
                            selvstendigNæringsdrivende.arbeidsforhold?.jobberNormaltTimer,
                            selvstendigNæringsdrivende.arbeidsforhold,
                        ),
                        VerdilisteElement(
                            label = "Næringsvirksomhet som du har lagt inn",
                            visningsVariant = "PUNKTLISTE",
                            verdiliste =
                                listOfNotNull(
                                    lagVerdiElement(
                                        "Navn på virksomheten: ",
                                        selvstendigNæringsdrivende.virksomhet.navnPåVirksomheten,
                                    ),
                                    lagVerdiElement(
                                        "Næringstype: ",
                                        selvstendigNæringsdrivende.virksomhet.næringstype,
                                    ),
                                    if (selvstendigNæringsdrivende.virksomhet.registrertINorge) {
                                        lagVerdiElement(
                                            "Registrert i Norge: ",
                                            "Organisasjonsnummer:  ${selvstendigNæringsdrivende.virksomhet.organisasjonsnummer}",
                                        )
                                    } else {
                                        lagVerdiElement(
                                            "Registrert i Land: ",
                                            selvstendigNæringsdrivende.virksomhet.registrertIUtlandet?.landnavn + " " +
                                                selvstendigNæringsdrivende.virksomhet.registrertIUtlandet?.landkode,
                                            selvstendigNæringsdrivende.virksomhet.registrertIUtlandet,
                                        )
                                    },
                                    lagVerdiElement(
                                        "Startet: ",
                                        DATE_TIME_FORMATTER.format(selvstendigNæringsdrivende.virksomhet.fraOgMed) + " - " +
                                            (
                                                selvstendigNæringsdrivende.virksomhet.tilOgMed?.let {
                                                    DATE_TIME_FORMATTER.format(
                                                        it,
                                                    )
                                                }
                                                    ?: "(Pågående)"
                                            ),
                                    ),
                                    lagVerdiElement(
                                        "Avsluttet",
                                        selvstendigNæringsdrivende.virksomhet.tilOgMed,
                                        selvstendigNæringsdrivende.virksomhet.tilOgMed,
                                    ),
                                    VerdilisteElement(
                                        label = "Næringstype: " + selvstendigNæringsdrivende.virksomhet.næringstype.beskrivelse,
                                        verdi =
                                            if (selvstendigNæringsdrivende.virksomhet.næringstype == Næringstype.FISKE) {
                                                if (selvstendigNæringsdrivende.virksomhet.fiskerErPåBladB) {
                                                    "(blad B)"
                                                } else {
                                                    "(ikke blad B)"
                                                }
                                            } else {
                                                null
                                            },
                                    ),
                                ),
                        ),
                        lagVerdiElement(
                            "Har du hatt varig endring i virksomheten?",
                            selvstendigNæringsdrivende.virksomhet.varigEndring,
                        ),
                        lagVerdiElement(
                            "Hva har du hatt i næringsresultat før skatt de siste 12 månedene?",
                            selvstendigNæringsdrivende.virksomhet.næringsinntekt,
                            selvstendigNæringsdrivende.virksomhet.næringsinntekt,
                        ),
                        selvstendigNæringsdrivende.virksomhet.varigEndring?.let { varigEndring ->
                            lagVerdiElement(
                                "Har du hatt en varig endring i noen av arbeidsforholdene," +
                                    " virksomhetene eller arbeidssituasjonen din de siste fire årene?",
                                "Ja",
                            )
                            lagVerdiElement("Dato for når varig endring:", varigEndring.dato)
                            lagVerdiElement("Næringsinntekt etter endringen", varigEndring.inntektEtterEndring)
                            lagVerdiElement("Beskrivelse av endring", varigEndring.forklaring)
                        },
                        lagVerdiElement(
                            "Har du begynt i arbeidslivet i løpet av de 3 siste ferdigliknede årene?",
                            selvstendigNæringsdrivende.virksomhet.yrkesaktivSisteTreFerdigliknedeÅrene?.let {
                                "Ja, ble yrkesaktiv ${it.oppstartsdato}"
                            } ?: "Nei",
                        ),
                        lagVerdiElement(
                            "Har du regnskapsfører?",
                            selvstendigNæringsdrivende.virksomhet.regnskapsfører?.let {
                                "Ja, ${it.navn}, telefon: ${it.telefon}"
                            } ?: "Nei",
                        ),
                        lagVerdiElement(
                            "Har du flere enn én næringsvirksomhet som er aktiv?",
                            konverterBooleanTilSvar(selvstendigNæringsdrivende.virksomhet.harFlereAktiveVirksomheter),
                        ),
                    ),
            )
        } ?: lagVerdiElement(
            "Selvstendig næringsdrivende",
            "Har ikke vært selvstending næringsdrivende i perioden det søkes om.",
        )

    fun mapOpptjeningIUtlandet(opptjeningIUtlandet: List<OpptjeningIUtlandet>): VerdilisteElement =
        opptjeningIUtlandet.takeIf { it.isNotEmpty() }?.let {
            VerdilisteElement(
                label =
                    "Har jobbet som arbeidstaker eller frilanser i et " +
                        "annet EØS-land i løpet av de 3 siste månedene før perioden en søker om?",
                visningsVariant = "PUNKTLISTE",
                verdiliste =
                    opptjeningIUtlandet.map { opptjeningIUtlandet ->
                        VerdilisteElement(
                            label =
                                "Jobbet i ${opptjeningIUtlandet.land.landnavn} som ${opptjeningIUtlandet.opptjeningType.pdfTekst}" +
                                    " hos ${opptjeningIUtlandet.navn} ${opptjeningIUtlandet.fraOgMed} - ${opptjeningIUtlandet.tilOgMed}",
                        )
                    },
            )
        } ?: VerdilisteElement(label = "Nei")

    fun jobbISøknadsPerioden(søknad: PilsSøknadMottatt): VerdilisteElement {
        val ingenArbeidsforholdRegistrert: Boolean = søknad.arbeidsgivere.isEmpty()
        val arbeidsgivere: List<Arbeidsgiver> = søknad.arbeidsgivere
        val frilansArbeidsforhold: Arbeidsforhold? = søknad.frilans?.arbeidsforhold
        return VerdilisteElement(
            label = "Jobb i søknadsperioden",
            verdiliste =
                listOfNotNull(
                    ingenArbeidsforholdRegistrert.takeIf { it }?.let {
                        lagVerdiElement(
                            "Har du jobbet i perioden?",
                            "Ingen arbeidsforhold er registrert i søknadsperioden",
                        )
                    },
                ) +
                    arbeidsgivere.map { arbeidsgiver ->
                        VerdilisteElement(
                            label =
                                arbeidsgiver.navn + " Orgnr: " +
                                    arbeidsgiver.organisasjonsnummer,
                            verdiliste = arbeidsForholdMapper(arbeidsgiver.arbeidsforhold),
                        )
                    } +
                    VerdilisteElement(
                        label = "Frilans",
                        verdiliste = arbeidsForholdMapper(frilansArbeidsforhold),
                    ) +
                    VerdilisteElement(
                        label = "Selvstendig næringsdrivende",
                        verdiliste = arbeidsForholdMapper(søknad.selvstendigNæringsdrivende?.arbeidsforhold),
                    ),
        )
    }

    fun arbeidsForholdMapper(arbeidsForhold: Arbeidsforhold?): List<VerdilisteElement> {
        arbeidsForhold ?: return listOfNotNull(VerdilisteElement(label = "Ikke registrert"))
        val arbeidsForholdTypet = mapArbeidsforholdTilType(arbeidsForhold)
        return listOfNotNull(
            VerdilisteElement(
                label = arbeidsForholdTypet.arbeidIPeriode.jobberIPerioden,
                visningsVariant = "PUNKTLISTE",
                verdiliste =
                    arbeidsForholdTypet.arbeidIPeriode.enkeltdagerPerMnd?.map { enkeltdag ->
                        VerdilisteElement(
                            label = enkeltdag.måned + " " + enkeltdag.år,
                            visningsVariant = "PUNKTLISTE",
                            verdiliste =
                                enkeltdag.enkeltdagerPerUke.flatMap { enkeltdagPerUke ->
                                    enkeltdagPerUke.dager.map { dag ->
                                        VerdilisteElement(
                                            label = "${dag.dag} ${dag.dato}: ${dag.tid}",
                                        )
                                    }
                                },
                        )
                    },
            ),
        )
    }

    fun mapMedlemskap(medlemskap: Medlemskap): VerdilisteElement =
        VerdilisteElement(
            label = "Medlemskap i folketrygden",
            verdiliste =
                listOf(
                    VerdilisteElement(
                        label = "Har du bodd i utlandet de siste 12 månedene?",
                        verdiliste =
                            mapUtenlandsOpphold(
                                medlemskap.harBoddIUtlandetSiste12Mnd,
                                medlemskap.utenlandsoppholdSiste12Mnd,
                            ),
                    ),
                    VerdilisteElement(
                        label = "Skal du bo i utlandet de neste 12 månedene?",
                        verdiliste =
                            mapUtenlandsOpphold(
                                medlemskap.skalBoIUtlandetNeste12Mnd,
                                medlemskap.utenlandsoppholdNeste12Mnd,
                            ),
                    ),
                ),
        )

    fun mapVerneplikt(harVærtEllerErVernepliktig: Boolean?): VerdilisteElement? =
        harVærtEllerErVernepliktig?.let {
            VerdilisteElement(
                label = "Verneplikt",
                verdiliste =
                    listOfNotNull(
                        lagVerdiElement(
                            "Utøvde du verneplikt på tidspunktet du søker pleiepenger fra?",
                            konverterBooleanTilSvar(harVærtEllerErVernepliktig),
                        ),
                    ),
            )
        }

    fun mapUtlandskNæring(utenlandskNæring: List<UtenlandskNæring>): VerdilisteElement? =
        VerdilisteElement(
            label = "Utenlandsk næring",
            verdiliste =
                listOfNotNull(
                    utenlandskNæring.takeIf { it.isNotEmpty() }?.let {
                        VerdilisteElement(
                            label = "Ja",
                            visningsVariant = "PUNKTLISTE",
                            verdiliste =
                                utenlandskNæring.map { naering ->
                                    VerdilisteElement(
                                        label =
                                            "${naering.navnPåVirksomheten.capitalizeName()} " +
                                                mapPeriodeTilString(naering.fraOgMed, naering.tilOgMed),
                                    )
                                },
                        )
                    },
                ),
        )

    fun mapUtenlandsOpphold(
        check: Boolean,
        utenlandsOpphold: List<Bosted>,
    ): List<VerdilisteElement> =
        listOf(
            if (check) {
                VerdilisteElement(
                    label = "Ja",
                    visningsVariant = "PUNKTLISTE",
                    verdiliste =
                        utenlandsOpphold.map { bosted ->
                            VerdilisteElement(
                                label =
                                    "${bosted.landnavn} " +
                                        "(${mapPeriodeTilString(bosted.fraOgMed, bosted.tilOgMed)})",
                            )
                        },
                )
            } else {
                VerdilisteElement(label = "Nei")
            },
        )

    fun mapVedlegg(søknad: PilsSøknadMottatt) {
        val harLastetOppId = søknad.opplastetIdVedleggId.isNotEmpty()
        val manglerNorskIdentitetsnummer = søknad.pleietrengende.norskIdentitetsnummer == null
        val harLastetOppLegeerklæring = søknad.vedleggId.isNotEmpty()

        val vedlegg =
            VerdilisteElement(
                label = "Vedlegg",
                verdiliste =
                    listOfNotNull(
                        VerdilisteElement(
                            label = "Har lastet opp kopi av ID til pleietrengende",
                            verdi = konverterBooleanTilSvar(harLastetOppId),
                        ),
                        VerdilisteElement(
                            label = "Har du lastet opp legeerklæring?",
                            verdi = konverterBooleanTilSvar(harLastetOppLegeerklæring),
                        ),
                    ),
            )
    }

    fun mapSamtykke(søknad: PilsSøknadMottatt): VerdilisteElement {
        val harForståttRettigheterOgPlikter = søknad.harForståttRettigheterOgPlikter
        val harBekreftetOpplysninger = søknad.harBekreftetOpplysninger
        return VerdilisteElement(
            label = "Samtykke fra deg",
            verdiliste =
                listOfNotNull(
                    lagVerdiElement(
                        "Har du forstått dine rettigheter og plikter?",
                        konverterBooleanTilSvar(harForståttRettigheterOgPlikter),
                    ),
                    lagVerdiElement(
                        "Har du bekreftet at opplysninger du har gitt er riktige?",
                        konverterBooleanTilSvar(harBekreftetOpplysninger),
                    ),
                ),
        )
    }

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

    fun mapPeriodeTilString(
        fraOgMed: LocalDate?,
        tilOgMed: LocalDate?,
    ): String = "${DATE_FORMATTER.format(fraOgMed)} - ${DATE_FORMATTER.format(tilOgMed)}"

    private fun elseIfSvar(
        svar: Boolean,
        svarFalse: String,
    ): String =
        if (svar) {
            "Ja"
        } else {
            svarFalse
        }
}
