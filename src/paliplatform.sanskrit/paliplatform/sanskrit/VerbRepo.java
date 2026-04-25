/*
 * VerbRepo.java
 *
 * Copyright (C) 2023-2026 J. R. Bhaddacak 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 */

package paliplatform.sanskrit;

import static paliplatform.sanskrit.SktConjugation.Pada;

import java.util.*;

/** 
 * Repository of Sanskrit verbs.
 * @author J.R. Bhaddacak
 * @version 4.2
 * @since 4.1
 */
public final class VerbRepo {
	public static Map<String, SktVerb> sktVerbMap = new LinkedHashMap<>();

	private VerbRepo() {
	}
	
	// static box to initialize all verbs
	static {

		sktVerbMap.put("añcati (√añc)", new SktVerb.Builder(1, "añc", 1, "bend")
				.citActForm("añcati")
				.presPassForm("añcyate", "acyate")
				.futForm("añciṣyati")
				.presCausForm("añcayati")
				.desForm(Pada.ACT, "añciciṣati")
				.perfForm(Pada.ACT, "ānañca")
				.perfParad(Pada.ACT, "jijīva")
				.perfSub(Pada.ACT, "ñc")
				.aorForm(Pada.ACT, "āñcīt")
				.infForm("añcitum")
				.absForm("añcitvā", "aktvā")
				.prefAbsForm("acya")
				.pppForm("añcita", "acita")
				.completed()
				.build());

		sktVerbMap.put("anakti (√añj)", new SktVerb.Builder(2, "añj", 7, "anoint")
				.citActForm("anakti")
				.citMidForm("anakte")
				.build());

		sktVerbMap.put("aṭati (√aṭ)", new SktVerb.Builder(3, "aṭ", 1, "wander")
				.citActForm("aṭati")
				.citMidForm("aṭate")
				.presPassForm("aṭyate")
				.futForm("aṭiṣyati")
				.presCausForm("āṭayati")
				.desForm(Pada.ACT, "aṭiṭiṣati")
				.intForm("aṭāṭyate")
				.perfForm(Pada.ACT, "āṭa")
				.perfParad(Pada.ACT, "jijīva")
				.perfSub(Pada.ACT, "āṭ")
				.aorForm(Pada.ACT, "āṭīt")
				.aorCausForm("āṭitat")
				.infForm("aṭitum")
				.absForm("aṭitvā")
				.pppForm("aṭita")
				.fppYaForm("aṭya")
				.completed()
				.build());

		sktVerbMap.put("atti (√ad)", new SktVerb.Builder(4, "ad", 2, "eat")
				.citActForm("atti")
				.presPassForm("adyate")
				.prefAbsForm("jagdhya")
				.futForm("atsyati")
				.presCausForm("ādayati")
				.desForm(Pada.ACT, "jighatsati")
				.perfForm(Pada.ACT, "āda")
				.perfParad(Pada.ACT, "jijīva")
				.perfSub(Pada.ACT, "ād")
				.aorForm(Pada.ACT, "aghasat")
				.aorCausForm("ādidat")
				.infForm("attum")
				.fppForm("attavya")
				.absForm("jagdhvā")
				.pppForm("jagdha")
				.fppNiyaForm("adanīya")
				.fppYaForm("adya")
				.completed()
				.build());

		sktVerbMap.put("aniti (√an)", new SktVerb.Builder(5, "an", 2, "breath")
				.citActForm("aniti")
				.presPassForm("anyate")
				.futForm("aniṣyati")
				.presCausForm("ānayati")
				.desForm(Pada.ACT, "aniniṣati")
				.perfForm(Pada.ACT, "āna")
				.perfParad(Pada.ACT, "jijīva")
				.perfSub(Pada.ACT, "ān")
				.aorForm(Pada.ACT, "ānīt")
				.aorPassForm("āni")
				.aorCausForm("āninat")
				.infForm("anitum")
				.absForm("ānitvā")
				.pppForm("anita")
				.fppYaForm("anīya")
				.completed()
				.build());

		sktVerbMap.put("arthayate (√arth)", new SktVerb.Builder(6, "arth", 10, "ask for")
				.citMidForm("arthayate")
				.midNormal()
				.build());

		sktVerbMap.put("arhati (√arh)", new SktVerb.Builder(7, "arh", 1, "deserve")
				.citActForm("arhati")
				.presPassForm("arhyate")
				.prefAbsForm("arghya")
				.futForm("arhiṣyati")
				.presCausForm("arhayati")
				.desForm(Pada.ACT, "arjihiṣati")
				.perfForm(Pada.ACT, "ānarha")
				.perfParad(Pada.ACT, "jijīva")
				.perfSub(Pada.ACT, "rh")
				.aorForm(Pada.ACT, "ārhīt")
				.aorPassForm("ārhi")
				.infForm("arhitum")
				.absForm("arhitvā")
				.pppForm("arhita")
				.fppNiyaForm("arhaṇīya")
				.completed()
				.build());

		sktVerbMap.put("avati (√av)", new SktVerb.Builder(8, "av", 1, "further")
				.citActForm("avati")
				.build());

		sktVerbMap.put("aśnute (√aś)", new SktVerb.Builder(9, "aś", 5, "obtain")
				.citMidForm("aśnute")
				.midNormal()
				.build());

		sktVerbMap.put("aśnāti (√aś)", new SktVerb.Builder(10, "aś", 9, "eat")
				.citActForm("aśnāti")
				.build());

		sktVerbMap.put("asti (√as)", new SktVerb.Builder(11, "as", 2, "be")
				.citActForm("asti")
				.perfForm(Pada.ACT, "āsa")
				.perfParad(Pada.ACT, "āsa")
				.build());

		sktVerbMap.put("asyati (√as)", new SktVerb.Builder(12, "as", 4, "throw")
				.citActForm("asyati")
				.build());

		sktVerbMap.put("āha (√ah)", new SktVerb.Builder(13, "ah", 1, "say")
				.citActForm("āha")
				.perfForm(Pada.ACT, "āha")
				.perfParad(Pada.ACT, "āha")
				.build());

		sktVerbMap.put("āpnoti (√āp)", new SktVerb.Builder(14, "āp", 5, "acquire")
				.citActForm("āpnoti")
				.presPassForm("āpyate")
				.futForm("āpsyati")
				.presCausForm("āpayati")
				.desForm(Pada.ACT, "īpsati")
				.perfForm(Pada.ACT, "āpa")
				.perfParad(Pada.ACT, "jijīva")
				.perfSub(Pada.ACT, "āp")
				.aorForm(Pada.ACT, "āpat")
				.aorPassForm("āpi")
				.aorCausForm("āpipat")
				.infForm("āptum")
				.fppForm("āptavya")
				.absForm("āptvā")
				.pppForm("āpta")
				.fppNiyaForm("āpanīya")
				.fppYaForm("āpya")
				.completed()
				.build());

		sktVerbMap.put("āste (√ās)", new SktVerb.Builder(15, "ās", 2, "sit")
				.citMidForm("āste")
				.midNormal()
				.build());

		sktVerbMap.put("eti (√i)", new SktVerb.Builder(16, "i", 2, "go")
				.addRootClass(1)
				.citActForm("eti")
				.build());

		sktVerbMap.put("inddhe (√indh)", new SktVerb.Builder(17, "indh", 7, "kindle")
				.citMidForm("inddhe")
				.midNormal()
				.build());

		sktVerbMap.put("icchati (√iṣ)", new SktVerb.Builder(18, "iṣ", 6, "desire")
				.citActForm("icchati")
				.presPassForm("iṣyate")
				.futForm("eṣiṣyati")
				.presCausForm("eṣayati")
				.desForm(Pada.ACT, "eṣiṣiṣati")
				.perfForm(Pada.ACT, "iyeṣa")
				.perfParad(Pada.ACT, "iyeṣa")
				.perfParad(Pada.MID, "īṣe")
				.aorForm(Pada.ACT, "aiṣīt")
				.aorPassForm("aiṣi")
				.aorCausForm("aiṣiṣat")
				.infForm("eṣitum", "eṣtum")
				.fppForm("eṣitavya", "eṣtavya")
				.absForm("iṣṭvā", "eṣitvā")
				.pppForm("iṣṭa")
				.fppNiyaForm("eṣaṇīya")
				.fppYaForm("eṣya")
				.completed()
				.build());

		sktVerbMap.put("iṣyati (√iṣ)", new SktVerb.Builder(19, "iṣ", 4, "send")
				.citActForm("iṣyati")
				.build());

		sktVerbMap.put("īkṣate (√īkṣ)", new SktVerb.Builder(20, "īkṣ", 1, "see")
				.citMidForm("īkṣate")
				.midNormal()
				.build());

		sktVerbMap.put("īṭṭe (√īḍ)", new SktVerb.Builder(21, "īḍ", 2, "praise")
				.citMidForm("īṭṭe")
				.midNormal()
				.build());

		sktVerbMap.put("īrte (√īr)", new SktVerb.Builder(22, "īr", 2, "move")
				.citMidForm("īrte")
				.midNormal()
				.build());

		sktVerbMap.put("īṣṭe (√īś)", new SktVerb.Builder(23, "īś", 2, "rule") .citMidForm("īṣṭe")
				.midNormal()
				.build());

		sktVerbMap.put("īṣate (√īṣ)", new SktVerb.Builder(24, "īṣ", 1, "flee")
				.citMidForm("īṣate")
				.midNormal()
				.build());

		sktVerbMap.put("ukṣati (√ukṣ)", new SktVerb.Builder(25, "ukṣ", 1, "sprinkle")
				.citActForm("ukṣati")
				.citMidForm("ukṣate")
				.build());

		sktVerbMap.put("uñchati (√uñch)", new SktVerb.Builder(26, "uñch", 1, "sweep")
				.addRootClass(6)
				.citActForm("uñchati")
				.build());

		sktVerbMap.put("unatti (√und)", new SktVerb.Builder(27, "und", 7, "moisten")
				.citActForm("unatti")
				.build());

		sktVerbMap.put("ubhnāti (√ubh)", new SktVerb.Builder(28, "ubh", 9, "confine")
				.addRootClass(6)
				.citActForm("ubhnāti")
				.build());

		sktVerbMap.put("oṣati (√uṣ)", new SktVerb.Builder(29, "uṣ", 1, "burn")
				.citActForm("oṣati")
				.build());

		sktVerbMap.put("ūhati (√ūh)", new SktVerb.Builder(30, "ūh", 1, "remove")
				.citActForm("ūhati")
				.build());

		sktVerbMap.put("ṛcchati (√ṛ)", new SktVerb.Builder(31, "ṛ", 1, "move")
				.addRootClass(3, 5)
				.citActForm("ṛcchati")
				.build());

		sktVerbMap.put("ṛcati (√ṛc)", new SktVerb.Builder(32, "ṛc", 6, "praise")
				.citActForm("ṛcati")
				.build());

		sktVerbMap.put("arjati (√ṛj)", new SktVerb.Builder(33, "ṛj", 1, "obtain")
				.citActForm("arjati")
				.citMidForm("arjate")
				.build());

		sktVerbMap.put("ṛdhnoti (√ṛdh)", new SktVerb.Builder(34, "ṛdh", 5, "thrive")
				.addRootClass(4)
				.citActForm("ṛdhnoti")
				.build());

		sktVerbMap.put("ṛṣati (√ṛṣ)", new SktVerb.Builder(35, "ṛṣ", 6, "push")
				.citActForm("ṛṣati")
				.build());

		sktVerbMap.put("edhate (√edh)", new SktVerb.Builder(36, "edh", 1, "thrive")
				.citMidForm("edhate")
				.midNormal()
				.build());

		sktVerbMap.put("kathayati (√kath)", new SktVerb.Builder(37, "kath", 10, "tell")
				.citActForm("kathayati")
				.citMidForm("kathayate")
				.presPassForm("kathyate")
				.prefAbsForm("kathayya")
				.futForm("kathayiṣyati")
				.presCausForm("kāthayati")
				.desForm(Pada.ACT, "cakathayiṣati")
				.perfPeriForm(Pada.ACT, "kathayām āsa")
				.aorForm(Pada.ACT, "acīkathat")
				.infForm("kathayitum")
				.absForm("kathayitvā")
				.pppForm("kathita")
				.fppNiyaForm("kathanīya")
				.completed()
				.build());

		sktVerbMap.put("kampate (√kamp)", new SktVerb.Builder(38, "kamp", 1, "tremble")
				.citMidForm("kampate")
				.citActForm("kampati")
				.midNormal()
				.build());

		sktVerbMap.put("kālayati (√kal)", new SktVerb.Builder(39, "kal", 10, "drive")
				.citActForm("kālayati")
				.citMidForm("kālayate")
				.build());

		sktVerbMap.put("kalayati (√kal)", new SktVerb.Builder(40, "kal", 10, "count")
				.citActForm("kalayati")
				.citMidForm("kalayate")
				.build());

		sktVerbMap.put("kasati (√kas)", new SktVerb.Builder(41, "kas", 1, "move")
				.citActForm("kasati")
				.build());

		sktVerbMap.put("kāṅkṣati (√kāṅkṣ)", new SktVerb.Builder(42, "kāṅkṣ", 1, "desire")
				.citActForm("kāṅkṣati")
				.build());

		sktVerbMap.put("kāśate (√kāś)", new SktVerb.Builder(43, "kāś", 1, "shine")
				.addRootClass(4)
				.citMidForm("kāśate")
				.altMidForm("kāśyate")
				.midNormal()
				.build());

		sktVerbMap.put("kupyati (√kap)", new SktVerb.Builder(44, "kap", 4, "be angry")
				.citActForm("kupyati")
				.build());

		sktVerbMap.put("karoti (√kṛ)", new SktVerb.Builder(45, "kṛ", 8, "do")
				.citActForm("karoti")
				.presPassForm("kriyate")
				.futForm("kariṣyati")
				.precForm(Pada.MID, "kṛṣīṣṭa")
				.presCausForm("kārayati")
				.desForm(Pada.ACT, "cikīrṣati")
				.perfForm(Pada.ACT, "cakāra")
				.perfParad(Pada.ACT, "cakāra")
				.perfParad(Pada.MID, "cakre")
				.aorForm(Pada.ACT, "akārṣīt")
				.aorPassForm("akāri")
				.aorCausForm("acīkarat")
				.infForm("kartum")
				.absForm("kṛtvā")
				.prefAbsForm("kṛtya")
				.pppForm("kṛta")
				.fppNiyaForm("karaṇīya")
				.fppYaForm("kārya")
				.completed()
				.build());

		sktVerbMap.put("kṛntati (√kṛt)", new SktVerb.Builder(46, "kṛt", 6, "cut")
				.citActForm("kṛntati")
				.build());

		sktVerbMap.put("kṛśyati (√kṛś)", new SktVerb.Builder(47, "kṛś", 4, "grow lean")
				.citActForm("kṛśyati")
				.build());

		sktVerbMap.put("karṣati (√kṛṣ)", new SktVerb.Builder(48, "kṛṣ", 1, "pull")
				.addRootClass(6)
				.citActForm("karṣati")
				.altActForm("kṛṣati")
				.altMeaning("plough")
				.build());

		sktVerbMap.put("kirati (√kṝ)", new SktVerb.Builder(49, "kṝ", 6, "strew")
				.citActForm("kirati")
				.build());

		sktVerbMap.put("kalpate (√kḷp)", new SktVerb.Builder(50, "kḷp", 1, "be able")
				.citMidForm("kalpate")
				.midNormal()
				.build());

		sktVerbMap.put("krāmati (√kram)", new SktVerb.Builder(51, "kram", 1, "step")
				.addRootClass(4)
				.citActForm("krāmati")
				.altActForm("krāmyati")
				.citMidForm("kramate")
				.build());

		sktVerbMap.put("krīṇāti (√krī)", new SktVerb.Builder(52, "krī", 9, "buy")
				.citActForm("krīṇāti")
				.build());

		sktVerbMap.put("krīḍati (√krīḍ)", new SktVerb.Builder(53, "krīḍ", 1, "play")
				.citActForm("krīḍati")
				.citMidForm("krīḍate")
				.build());

		sktVerbMap.put("krudhyati (√krudh)", new SktVerb.Builder(54, "krudh", 4, "be angry")
				.citActForm("krudhyati")
				.build());

		sktVerbMap.put("krośati (√kruś)", new SktVerb.Builder(55, "kruś", 1, "cry out")
				.citActForm("krośati")
				.build());

		sktVerbMap.put("kliśnāti (√kliś)", new SktVerb.Builder(56, "kliś", 1, "suffer")
				.citActForm("kliśnāti")
				.build());

		sktVerbMap.put("kṣaṇoti (√kṣaṇ)", new SktVerb.Builder(57, "kṣaṇ", 8, "wound")
				.citActForm("kṣaṇoti")
				.citMidForm("kṣaṇote")
				.build());

		sktVerbMap.put("kṣamati (√kṣam)", new SktVerb.Builder(58, "kṣam", 1, "endure")
				.addRootClass(4)
				.citActForm("kṣamati")
				.citMidForm("kṣamate")
				.build());

		sktVerbMap.put("kṣarati (√kṣar)", new SktVerb.Builder(59, "kṣar", 1, "flow")
				.citActForm("kṣarati")
				.citMidForm("kṣarate")
				.build());

		sktVerbMap.put("kṣālayati (√kṣal)", new SktVerb.Builder(60, "kṣal", 10, "wash")
				.citActForm("kṣālayati")
				.build());

		sktVerbMap.put("kṣiṇoti (√kṣi)", new SktVerb.Builder(61, "kṣi", 5, "destroy")
				.addRootClass(9)
				.citActForm("kṣiṇoti")
				.build());

		sktVerbMap.put("kṣipati (√kṣip)", new SktVerb.Builder(62, "kṣip", 6, "throw")
				.addRootClass(4)
				.citActForm("kṣipati")
				.citMidForm("kṣipate")
				.altActForm("kṣipyati")
				.altMidForm("kṣipyate")
				.build());

		sktVerbMap.put("kṣuṇatti (√kṣud)", new SktVerb.Builder(63, "kṣud", 7, "shatter")
				.citActForm("kṣuṇatti")
				.build());

		sktVerbMap.put("kṣudhyati (√kṣudh)", new SktVerb.Builder(64, "kṣudh", 4, "hunger")
				.citActForm("kṣudhyati")
				.build());

		sktVerbMap.put("khaṇḍayati (√khaṇḍ)", new SktVerb.Builder(65, "khaṇḍ", 10, "break")
				.citActForm("khaṇḍayati")
				.build());

		sktVerbMap.put("khanati (√khan)", new SktVerb.Builder(66, "khan", 1, "dig")
				.citActForm("khanati")
				.citMidForm("khanate")
				.build());

		sktVerbMap.put("khādati (√khād)", new SktVerb.Builder(67, "khād", 1, "eat")
				.citActForm("khādati")
				.citMidForm("khādate")
				.build());

		sktVerbMap.put("khindati (√khid)", new SktVerb.Builder(68, "khid", 6, "afflict")
				.addRootClass(7)
				.citActForm("khindati")
				.build());

		sktVerbMap.put("khyāti (√khyā)", new SktVerb.Builder(69, "khyā", 2, "be known")
				.citActForm("khyāti")
				.build());

		sktVerbMap.put("gaṇayati (√gaṇ)", new SktVerb.Builder(70, "gaṇ", 10, "count")
				.citActForm("gaṇayati")
				.citMidForm("gaṇayate")
				.build());

		sktVerbMap.put("gadati (√gad)", new SktVerb.Builder(71, "gad", 1, "speak")
				.citActForm("gadati")
				.build());

		sktVerbMap.put("gacchati (√gam)", new SktVerb.Builder(72, "gam", 1, "go")
				.citActForm("gacchati")
				.citMidForm("gacchate")
				.presPassForm("gamyate")
				.futForm("gamiṣyati")
				.presCausForm("gamayati")
				.desForm(Pada.ACT, "jigamiṣati")
				.perfForm(Pada.ACT, "jagāma")
				.perfParad(Pada.ACT, "nināya")
				.perfParad(Pada.MID, "ninye")
				.perfSub(Pada.ACT, "ām", "am", "m")
				.perfSub(Pada.MID, "m")
				.aorForm(Pada.ACT, "agamat")
				.aorPassForm("agāmi")
				.aorCausForm("ajīgamat")
				.infForm("gantum")
				.fppForm("gantavya")
				.absForm("gatvā")
				.pppForm("gata")
				.fppNiyaForm("gamanīya")
				.fppYaForm("gamya")
				.completed()
				.build());

		sktVerbMap.put("garjati (√garj)", new SktVerb.Builder(73, "garj", 1, "roar")
				.citActForm("garjati")
				.build());

		sktVerbMap.put("garhate (√garh)", new SktVerb.Builder(74, "garh", 1, "blame")
				.citMidForm("garhate")
				.citActForm("garhati")
				.midNormal()
				.build());

		sktVerbMap.put("galati (√gal)", new SktVerb.Builder(75, "gal", 1, "fall")
				.citActForm("galati")
				.build());

		sktVerbMap.put("gāti (√gā)", new SktVerb.Builder(76, "gā", 2, "go")
				.citActForm("gāti")
				.citMidForm("gāte")
				.build());

		sktVerbMap.put("gāhate (√gāh)", new SktVerb.Builder(77, "gāh", 1, "plunge")
				.citMidForm("gāhate")
				.citActForm("gāhati")
				.midNormal()
				.build());

		sktVerbMap.put("gavate (√gu)", new SktVerb.Builder(78, "gu", 1, "proclaim")
				.citMidForm("gavate")
				.midNormal()
				.build());

		sktVerbMap.put("gopāyati (√gup)", new SktVerb.Builder(79, "gup", 1, "guard")
				.citActForm("gopāyati")
				.build());

		sktVerbMap.put("gūhati (√guh)", new SktVerb.Builder(80, "guh", 1, "conceal")
				.citActForm("gūhati")
				.citMidForm("gūhate")
				.build());

		sktVerbMap.put("gṛdhyati (√gṛdh)", new SktVerb.Builder(81, "gṛdh", 4, "covet")
				.citActForm("gṛdhyati")
				.build());

		sktVerbMap.put("girati (√gṝ)", new SktVerb.Builder(82, "gṝ", 6, "swallow")
				.citActForm("girati")
				.citMidForm("girate")
				.build());

		sktVerbMap.put("gilati (√gṝ)", new SktVerb.Builder(82, "gṝ", 6, "swallow")
				.citActForm("gilati")
				.citMidForm("gilate")
				.build());

		sktVerbMap.put("gṛṇāti (√gṝ)", new SktVerb.Builder(83, "gṝ", 9, "invoke")
				.citActForm("gṛṇāti")
				.citMidForm("gṛṇāte")
				.build());

		sktVerbMap.put("gāyati (√gai)", new SktVerb.Builder(84, "gai", 1, "sing")
				.citActForm("gāyati")
				.build());

		sktVerbMap.put("gopāyati (√gopā)", new SktVerb.Builder(85, "gopā", 1, "guard")
				.citActForm("gopāyati")
				.build());

		sktVerbMap.put("grathnāti (√granth)", new SktVerb.Builder(86, "granth", 9, "compose")
				.addRootClass(1)
				.citActForm("grathnāti")
				.build());

		sktVerbMap.put("grasati (√gras)", new SktVerb.Builder(87, "gras", 1, "swallow")
				.citActForm("grasati")
				.citMidForm("grasate")
				.build());

		sktVerbMap.put("gṛhṇāti (√grah)", new SktVerb.Builder(88, "grah", 9, "seize")
				.citActForm("gṛhṇāti")
				.citMidForm("gṛhṇāte")
				.build());

		sktVerbMap.put("ghoṣati (√ghuṣ)", new SktVerb.Builder(89, "ghuṣ", 1, "sound")
				.citActForm("ghoṣati")
				.build());

		sktVerbMap.put("gharati (√ghṛ)", new SktVerb.Builder(90, "ghṛ", 1, "sprinkle")
				.addRootClass(3)
				.citActForm("gharati")
				.altActForm("jigharti")
				.build());

		sktVerbMap.put("jighrati (√ghrā)", new SktVerb.Builder(91, "ghrā", 1, "smell")
				.citActForm("jighrati")
				.build());

		// see Deshpande p.208
		sktVerbMap.put("caṣṭe (√cakṣ)", new SktVerb.Builder(92, "cakṣ", 2, "tell")
				.citMidForm("caṣṭe")
				.midNormal()
				.build());

		sktVerbMap.put("camati (√cam)", new SktVerb.Builder(93, "cam", 1, "sip")
				.citActForm("camati")
				.build());

		sktVerbMap.put("carati (√car)", new SktVerb.Builder(94, "car", 1, "go")
				.citActForm("carati")
				.build());

		sktVerbMap.put("carvati (√carv)", new SktVerb.Builder(95, "carv", 1, "chew")
				.citActForm("carvati")
				.build());

		sktVerbMap.put("calati (√cal)", new SktVerb.Builder(96, "cal", 1, "move")
				.citActForm("calati")
				.build());

		sktVerbMap.put("cinoti (√ci)", new SktVerb.Builder(97, "ci", 5, "gather")
				.citActForm("cinoti")
				.citMidForm("cinote")
				.build());

		sktVerbMap.put("cetati (√cit)", new SktVerb.Builder(98, "cit", 1, "consider")
				.citActForm("cetati")
				.build());

		sktVerbMap.put("cintayati (√cint)", new SktVerb.Builder(99, "cint", 10, "think")
				.citActForm("cintayati")
				.citMidForm("cintayate")
				.build());

		sktVerbMap.put("codayati (√cud)", new SktVerb.Builder(100, "cud", 10, "impel")
				.citActForm("codayati")
				.citMidForm("codayate")
				.build());

		sktVerbMap.put("corayati (√cur)", new SktVerb.Builder(101, "cur", 10, "steal")
				.citActForm("corayati")
				.citMidForm("corayate")
				.build());

		sktVerbMap.put("cṛtati (√cṛt)", new SktVerb.Builder(102, "cṛt", 6, "fasten")
				.citActForm("cṛtati")
				.altActForm("cṛntati")
				.build());

		sktVerbMap.put("ceṣṭati (√ceṣṭ)", new SktVerb.Builder(103, "ceṣṭ", 1, "act")
				.citActForm("ceṣṭati")
				.citMidForm("ceṣṭate")
				.build());

		sktVerbMap.put("cyavate (√cyu)", new SktVerb.Builder(104, "cyu", 1, "fall")
				.citMidForm("cyavate")
				.citActForm("cyavati")
				.midNormal()
				.build());

		sktVerbMap.put("chādayati (√chad)", new SktVerb.Builder(105, "chad", 10, "cover")
				.citActForm("chādayati")
				.citMidForm("chādayate")
				.build());

		sktVerbMap.put("chinatti (√chid)", new SktVerb.Builder(106, "chid", 7, "cut")
				.citActForm("chinatti")
				.build());

		sktVerbMap.put("jāyate (√jan)", new SktVerb.Builder(107, "jan", 4, "be born")
				.citMidForm("jāyate")
				.midNormal()
				.presPassForm("janyate")
				.futForm("janiṣyate")
				.presCausForm("janayati")
				.desForm(Pada.MID, "jijaniṣate")
				.perfForm(Pada.ACT, "jajāya") // needed but not used
				.perfForm(Pada.MID, "jajñe")
				.perfParad(Pada.MID, "ninye")
				.perfSub(Pada.MID, "ñ")
				.aorForm(Pada.ACT, "ajānīt") // needed but not used
				.aorForm(Pada.MID, "ajaniṣṭa")
				.aorCausForm("ajījanat")
				.infForm("janitum")
				.fppForm("janitavya")
				.absForm("janitvā")
				.pppForm("jāta")
				.fppYaForm("janya")
				.completed()
				.build());

		sktVerbMap.put("jalpati (√jalp)", new SktVerb.Builder(108, "jalp", 1, "murmur")
				.citActForm("jalpati")
				.build());

		sktVerbMap.put("jāgarti (√jāgṛ)", new SktVerb.Builder(109, "jāgṛ", 2, "wake")
				.citActForm("jāgarti")
				.build());

		sktVerbMap.put("jayati (√ji)", new SktVerb.Builder(110, "ji", 1, "conquer")
				.citActForm("jayati")
				.citMidForm("jayate")
				.build());

		sktVerbMap.put("jinvati (√jinv)", new SktVerb.Builder(111, "jinv", 1, "hasten")
				.citActForm("jinvati")
				.build());

		sktVerbMap.put("jīvati (√jīv)", new SktVerb.Builder(112, "jīv", 1, "live")
				.citActForm("jīvati")
				.citMidForm("jīvate")
				.build());

		sktVerbMap.put("juṣate (√juṣ)", new SktVerb.Builder(113, "juṣ", 6, "relish")
				.citMidForm("juṣate")
				.citActForm("juṣati")
				.midNormal()
				.build());

		sktVerbMap.put("jīryati (√jṝ)", new SktVerb.Builder(114, "jṝ", 4, "decay")
				.citActForm("jīryati")
				.citMidForm("jīryate")
				.build());

		sktVerbMap.put("jānāti (√jñā)", new SktVerb.Builder(115, "jñā", 9, "know")
				.citActForm("jānāti")
				.citMidForm("jānīte")
				.presPassForm("jñāyate")
				.futForm("jñāsyati")
				.presCausForm("jñāpayati")
				.desForm(Pada.ACT, "jijñāsati")
				.perfForm(Pada.ACT, "jajñau")
				.perfParad(Pada.ACT, "dadhau")
				.perfParad(Pada.MID, "dadhe")
				.aorForm(Pada.ACT, "ajñāsīt")
				.aorForm(Pada.MID, "ajñāsta")
				.aorPassForm("ajñāyi")
				.aorCausForm("ajijñapat")
				.infForm("jñātum")
				.fppForm("jñātavya")
				.absForm("jñātvā")
				.pppForm("jñāta")
				.fppYaForm("jñeya")
				.completed()
				.build());

		sktVerbMap.put("jināti (√jyā)", new SktVerb.Builder(116, "jyā", 9, "overpower")
				.citActForm("jināti")
				.build());

		sktVerbMap.put("jvalati (√jval)", new SktVerb.Builder(117, "jval", 1, "blaze")
				.citActForm("jvalati")
				.citMidForm("jvalate")
				.build());

		sktVerbMap.put("ḍhaukate (√ḍhauk)", new SktVerb.Builder(118, "ḍhauk", 1, "approach")
				.citMidForm("ḍhaukate")
				.midNormal()
				.build());

		sktVerbMap.put("takṣati (√takṣ)", new SktVerb.Builder(119, "takṣ", 1, "hew")
				.addRootClass(5)
				.citActForm("takṣati")
				.citMidForm("takṣate")
				.build());

		sktVerbMap.put("tāḍayati (√taḍ)", new SktVerb.Builder(120, "taḍ", 10, "hit")
				.citActForm("tāḍayati")
				.citMidForm("tāḍayate")
				.build());

		sktVerbMap.put("tanoti (√tan)", new SktVerb.Builder(121, "tan", 8, "stretch")
				.citActForm("tanoti")
				.citMidForm("tanote")
				.build());

		sktVerbMap.put("tapati (√tap)", new SktVerb.Builder(122, "tap", 1, "burn")
				.citActForm("tapati")
				.citMidForm("tapate")
				.build());

		sktVerbMap.put("tāmyati (√tam)", new SktVerb.Builder(123, "tam", 4, "faint")
				.citActForm("tāmyati")
				.build());

		sktVerbMap.put("tarkayati (√tark)", new SktVerb.Builder(124, "tark", 10, "infer")
				.citActForm("tarkayati")
				.build());

		sktVerbMap.put("tejayati (√tij)", new SktVerb.Builder(125, "tij", 10, "sharpen")
				.citActForm("tejayati")
				.build());

		sktVerbMap.put("tudati (√tud)", new SktVerb.Builder(126, "tud", 6, "hit")
				.citActForm("tudati")
				.citMidForm("tudate")
				.build());

		sktVerbMap.put("turati (√tur)", new SktVerb.Builder(127, "tur", 6, "hasten")
				.addRootClass(3)
				.citActForm("turati")
				.citMidForm("turate")
				.build());

		sktVerbMap.put("tuṣyati (√tul)", new SktVerb.Builder(128, "tul", 10, "weigh")
				.citActForm("tuṣyati")
				.build());

		sktVerbMap.put("tolayati (√tuṣ)", new SktVerb.Builder(129, "tuṣ", 4, "be satisfied")
				.citActForm("tolayati")
				.citMidForm("tolayate")
				.build());

		sktVerbMap.put("tṛṇatti (√tṛd)", new SktVerb.Builder(130, "tṛd", 7, "split")
				.citActForm("tṛṇatti")
				.build());

		sktVerbMap.put("tṛpyati (√tṛp)", new SktVerb.Builder(131, "tṛp", 4, "be satisfied")
				.citActForm("tṛpyati")
				.build());

		sktVerbMap.put("tṛṣyati (√tṛṣ)", new SktVerb.Builder(132, "tṛṣ", 4, "thirst")
				.citActForm("tṛṣyati")
				.build());

		sktVerbMap.put("tarati (√tṝ)", new SktVerb.Builder(133, "tṝ", 1, "cross over")
				.citActForm("tarati")
				.build());

		sktVerbMap.put("tyajati (√tyaj)", new SktVerb.Builder(134, "tyaj", 1, "leave")
				.citActForm("tyajati")
				.build());

		sktVerbMap.put("trasati (√tras)", new SktVerb.Builder(135, "tras", 1, "tremble")
				.addRootClass(4)
				.citActForm("trasati")
				.altActForm("trasyati")
				.build());

		sktVerbMap.put("trāti (√trā/trai)", new SktVerb.Builder(136, "trā", 2, "rescue")
				.addRootClass(4)
				.altRootName("trai")
				.citActForm("trāti")
				.build());

		sktVerbMap.put("tvarate (√tvar)", new SktVerb.Builder(137, "tvar", 1, "hasten")
				.citMidForm("tvarate")
				.citActForm("tvarati")
				.midNormal()
				.build());

		sktVerbMap.put("daśati (√daś/daṃś)", new SktVerb.Builder(138, "daś", 1, "bite")
				.altRootName("daṃś")
				.citActForm("daśati")
				.altActForm("daṃśati")
				.build());

		sktVerbMap.put("dakṣati (√dakṣ)", new SktVerb.Builder(139, "dakṣ", 1, "be able")
				.citActForm("dakṣati")
				.citMidForm("dakṣate")
				.build());

		sktVerbMap.put("daṇḍayati (√daṇḍ)", new SktVerb.Builder(140, "daṇḍ", 10, "punish")
				.citActForm("daṇḍayati")
				.citMidForm("daṇḍayate")
				.build());

		sktVerbMap.put("dāmyati (√dam)", new SktVerb.Builder(141, "dam", 4, "tame")
				.citActForm("dāmyati")
				.build());

		sktVerbMap.put("dabhati (√dambh)", new SktVerb.Builder(142, "dambh", 1, "deceive")
				.citActForm("dabhati")
				.build());

		sktVerbMap.put("dayate (√day)", new SktVerb.Builder(143, "day", 1, "pity")
				.citMidForm("dayate")
				.midNormal()
				.build());

		sktVerbMap.put("dasyati (√das)", new SktVerb.Builder(144, "das", 4, "lack")
				.citActForm("dasyati")
				.build());

		sktVerbMap.put("dahati (√dah)", new SktVerb.Builder(145, "dah", 1, "burn")
				.citActForm("dahati")
				.citMidForm("dahate")
				.build());

		sktVerbMap.put("dadāti (√dā)", new SktVerb.Builder(146, "dā", 3, "give")
				.citActForm("dadāti")
				.citMidForm("dadāte")
				.build());

		sktVerbMap.put("dāti (√dā)", new SktVerb.Builder(147, "dā", 2, "cut")
				.citActForm("dāti")
				.build());

		sktVerbMap.put("dīvyati (√div)", new SktVerb.Builder(148, "div", 4, "play")
				.citActForm("dīvyati")
				.build());

		sktVerbMap.put("devati (√div)", new SktVerb.Builder(149, "div", 1, "lament")
				.citActForm("devati")
				.build());

		sktVerbMap.put("diśati (√diś)", new SktVerb.Builder(150, "diś", 6, "show")
				.citActForm("diśati")
				.citMidForm("diśate")
				.build());

		sktVerbMap.put("degdhi (√dih)", new SktVerb.Builder(151, "dih", 2, "smear")
				.citActForm("degdhi")
				.citMidForm("degdhe")
				.build());

		sktVerbMap.put("dīkṣate (√dīkṣ)", new SktVerb.Builder(152, "dīkṣ", 1, "consecrate")
				.citMidForm("dīkṣate")
				.midNormal()
				.build());

		sktVerbMap.put("dīpyate (√dīp)", new SktVerb.Builder(153, "dīp", 4, "blaze")
				.citMidForm("dīpyate")
				.citActForm("dīpyati")
				.midNormal()
				.build());

		sktVerbMap.put("dunoti (√du)", new SktVerb.Builder(154, "du", 5, "suffer")
				.citActForm("dunoti")
				.build());

		sktVerbMap.put("dolayati (√dul)", new SktVerb.Builder(155, "dul", 10, "swing")
				.citActForm("dolayati")
				.build());

		sktVerbMap.put("duṣyati (√duṣ)", new SktVerb.Builder(156, "duṣ", 4, "spoil")
				.citActForm("duṣyati")
				.build());

		sktVerbMap.put("dogdhi (√duh)", new SktVerb.Builder(157, "duh", 2, "milk")
				.citActForm("dogdhi")
				.citMidForm("dugdhe")
				.build());

		sktVerbMap.put("driyate (√dṛ)", new SktVerb.Builder(158, "dṛ", 6, "heed")
				.citMidForm("driyate")
				.midNormal()
				.build());

		sktVerbMap.put("dṛpyati (√dṛp)", new SktVerb.Builder(159, "dṛp", 4, "be proud")
				.citActForm("dṛpyati")
				.build());

		sktVerbMap.put("paśyati (√dṛś)", new SktVerb.Builder(160, "dṛś", 1, "see")
				.citActForm("paśyati")
				.build());

		sktVerbMap.put("dṛṃhati (√dṛh/dṛṃh)", new SktVerb.Builder(161, "dṛh", 1, "establish")
				.altRootName("dṛṃh")
				.citActForm("dṛṃhati")
				.citMidForm("dṛṃhate")
				.build());

		sktVerbMap.put("dṛṇāti (√dṝ)", new SktVerb.Builder(162, "dṝ", 9, "tear")
				.citActForm("dṛṇāti")
				.build());

		sktVerbMap.put("dyotate (√dyut)", new SktVerb.Builder(163, "dyut", 1, "gleam")
				.citMidForm("dyotate")
				.midNormal()
				.build());

		sktVerbMap.put("drāti (√drā)", new SktVerb.Builder(164, "drā", 2, "run")
				.citActForm("drāti")
				.build());

		sktVerbMap.put("dravati (√dru)", new SktVerb.Builder(165, "dru", 1, "run")
				.citActForm("dravati")
				.citMidForm("dravate")
				.build());

		sktVerbMap.put("druhyati (√druh)", new SktVerb.Builder(166, "druh", 4, "offend")
				.citActForm("druhyati")
				.citMidForm("druhyate")
				.build());

		sktVerbMap.put("dveṣṭi (√dviṣ)", new SktVerb.Builder(167, "dviṣ", 2, "hate")
				.citActForm("dveṣṭi")
				.build());

		sktVerbMap.put("dadhāti (√dhā)", new SktVerb.Builder(168, "dhā", 3, "put")
				.citActForm("dadhāti")
				.citMidForm("dadhāte")
				.build());

		sktVerbMap.put("dhāvati (√dhāv)", new SktVerb.Builder(169, "dhāv", 1, "rinse")
				.citActForm("dhāvati")
				.citMidForm("dhāvate")
				.build());

		sktVerbMap.put("dhunoti (√dhu)", new SktVerb.Builder(170, "dhu", 5, "shake")
				.citActForm("dhunoti")
				.citMidForm("dhunote")
				.build());

		sktVerbMap.put("dharati (√dhṛ)", new SktVerb.Builder(171, "dhṛ", 1, "bear")
				.citActForm("dharati")
				.citMidForm("dharate")
				.build());

		sktVerbMap.put("dhṛṣṇoti (√dhṛṣ)", new SktVerb.Builder(172, "dhṛṣ", 5, "dare")
				.citActForm("dhṛṣṇoti")
				.build());

		sktVerbMap.put("dhayati (√dhe)", new SktVerb.Builder(173, "dhe", 1, "suck")
				.citActForm("dhayati")
				.build());

		sktVerbMap.put("dhamati (√dhmā)", new SktVerb.Builder(174, "dhmā", 1, "blow")
				.citActForm("dhamati")
				.build());

		sktVerbMap.put("dhyāti (√dhyai)", new SktVerb.Builder(175, "dhyai", 1, "ponder")
				.addRootClass(2)
				.citActForm("dhyāti")
				.citMidForm("dhyāte")
				.altActForm("dhyāyati")
				.altMidForm("dhyāyate")
				.build());

		sktVerbMap.put("dhrajati (√dhraj)", new SktVerb.Builder(176, "dhraj", 1, "advance")
				.citActForm("dhrajati")
				.altActForm("dhrañjati")
				.build());

		sktVerbMap.put("dhvaṃsati (√dhvaṃs)", new SktVerb.Builder(177, "dhvaṃs", 1, "perish")
				.citActForm("dhvaṃsati")
				.citMidForm("dhvaṃsate")
				.build());

		sktVerbMap.put("dhvanati (√dhvan)", new SktVerb.Builder(178, "dhvan", 1, "resound")
				.citActForm("dhvanati")
				.build());

		sktVerbMap.put("dhvarati (√dhvṛ)", new SktVerb.Builder(179, "dhvaṛ", 1, "bend")
				.citActForm("dhvarati")
				.build());

		sktVerbMap.put("nakṣati (√nakṣ)", new SktVerb.Builder(180, "nakṣ", 1, "attain")
				.citActForm("nakṣati")
				.citMidForm("nakṣate")
				.build());

		sktVerbMap.put("nadati (√nad)", new SktVerb.Builder(181, "nad", 1, "roar")
				.citActForm("nadati")
				.build());

		sktVerbMap.put("nandati (√nand)", new SktVerb.Builder(182, "nand", 1, "rejoice")
				.citActForm("nandati")
				.citMidForm("nandate")
				.build());

		sktVerbMap.put("nabhate (√nabh)", new SktVerb.Builder(183, "nabh", 1, "burst")
				.citMidForm("nabhate")
				.midNormal()
				.build());

		sktVerbMap.put("namati (√nam)", new SktVerb.Builder(184, "nam", 1, "bow")
				.citActForm("namati")
				.citMidForm("namate")
				.build());

		sktVerbMap.put("naśyati (√naś)", new SktVerb.Builder(185, "naś", 4, "perish")
				.citActForm("naśyati")
				.build());

		sktVerbMap.put("nahyati (√nah)", new SktVerb.Builder(186, "nah", 4, "bind")
				.citActForm("nahyati")
				.citMidForm("nahyate")
				.build());

		sktVerbMap.put("nāthati (√nāth)", new SktVerb.Builder(187, "nāth", 1, "implore")
				.citActForm("nāthati")
				.citMidForm("nāthate")
				.build());

		sktVerbMap.put("nindati (√nind)", new SktVerb.Builder(188, "nind", 1, "blame")
				.citActForm("nindati")
				.build());

		sktVerbMap.put("nayati (√nī)", new SktVerb.Builder(189, "nī", 1, "lead")
				.citActForm("nayati")
				.citMidForm("nayate")
				.presPassForm("nīyate")
				.futForm("neṣyati")
				.presCausForm("nāyayati")
				.desForm(Pada.ACT, "ninīṣati")
				.intForm("nenīyate")
				.perfForm(Pada.ACT, "nināya")
				.perfParad(Pada.ACT, "nināya")
				.perfParad(Pada.MID, "ninye")
				.aorForm(Pada.ACT, "anaiṣīt")
				.aorForm(Pada.MID, "aneṣṭa")
				.aorPassForm("anāyi")
				.aorCausForm("anīnayat")
				.infForm("netum")
				.fppForm("netavya")
				.absForm("nītvā")
				.pppForm("nīta")
				.fppYaForm("neya")
				.completed()
				.build());

		sktVerbMap.put("nauti (√nu)", new SktVerb.Builder(190, "nu", 2, "praise")
				.citActForm("nauti")
				.build());

		sktVerbMap.put("nudati (√nud)", new SktVerb.Builder(191, "nud", 6, "push")
				.citActForm("nudati")
				.citMidForm("nudate")
				.build());

		sktVerbMap.put("nṛtyati (√nṛt)", new SktVerb.Builder(192, "nṛt", 4, "dance")
				.citActForm("nṛtyati")
				.citMidForm("nṛtyate")
				.build());

		sktVerbMap.put("pacati (√pac)", new SktVerb.Builder(193, "pac", 1, "cook")
				.citActForm("pacati")
				.citMidForm("pacate")
				.presPassForm("pacyate")
				.futForm("pakṣyati")
				.presCausForm("pācayati")
				.desForm(Pada.ACT, "pipakṣati")
				.perfForm(Pada.ACT, "papāca")
				.perfParad(Pada.ACT, "tatāna")
				.perfParad(Pada.MID, "tene")
				.perfSub(Pada.ACT, "pāc", "pan")
				.aorForm(Pada.ACT, "apākṣīt")
				.aorPassForm("apāci")
				.aorCausForm("apīpacat")
				.infForm("paktum")
				.fppForm("paktavya")
				.absForm("paktvā")
				.pppForm("pakva")
				.completed()
				.build());

		sktVerbMap.put("paṭati (√pac)", new SktVerb.Builder(194, "paṭ", 1, "split")
				.citActForm("paṭati")
				.build());

		sktVerbMap.put("paṭhati (√paṭh)", new SktVerb.Builder(195, "paṭh", 1, "read")
				.citActForm("paṭhati")
				.build());

		sktVerbMap.put("paṇate (√paṇ)", new SktVerb.Builder(196, "paṇ", 1, "bargain")
				.citMidForm("paṇate")
				.midNormal()
				.build());

		sktVerbMap.put("patati (√pat)", new SktVerb.Builder(197, "pat", 1, "fall")
				.citActForm("patati")
				.citMidForm("patate")
				.build());

		sktVerbMap.put("padyate (√pad)", new SktVerb.Builder(198, "pad", 4, "go")
				.citMidForm("padyate")
				.citActForm("padyati")
				.midNormal()
				.build());

		sktVerbMap.put("palāyate (√palāy)", new SktVerb.Builder(199, "palāy", 1, "flee")
				.citMidForm("palāyate")
				.citActForm("palāyati")
				.midNormal()
				.build());

		sktVerbMap.put("paśyati (√paś)", new SktVerb.Builder(200, "paś", 1, "see")
				.citActForm("paśyati")
				.citMidForm("paśyate")
				.build());

		sktVerbMap.put("pibati (√pā)", new SktVerb.Builder(201, "pā", 1, "drink")
				.citActForm("pibati")
				.citMidForm("pibate")
				.build());

		sktVerbMap.put("pāti (√pā)", new SktVerb.Builder(202, "pā", 2, "protect")
				.citActForm("pāti")
				.build());

		sktVerbMap.put("pinvati (√pinv)", new SktVerb.Builder(203, "pinv", 1, "swell")
				.citActForm("pinvati")
				.build());

		sktVerbMap.put("piśati (√piś)", new SktVerb.Builder(204, "piś", 6, "adorn")
				.citActForm("piśati")
				.altActForm("piṃśati")
				.build());

		sktVerbMap.put("pinaṣṭi (√piṣ)", new SktVerb.Builder(205, "piṣ", 7, "grind")
				.citActForm("pinaṣṭi")
				.build());

		sktVerbMap.put("pīḍayati (√pīḍ)", new SktVerb.Builder(206, "pīḍ", 10, "torment")
				.citActForm("pīḍayati")
				.citMidForm("pīḍayate")
				.build());

		sktVerbMap.put("puṣṇāti (√puṣ)", new SktVerb.Builder(207, "puṣ", 9, "thrive")
				.addRootClass(4, 1)
				.citActForm("puṣṇāti")
				.build());

		sktVerbMap.put("punāti (√pū)", new SktVerb.Builder(208, "pū", 9, "purify")
				.addRootClass(1)
				.citActForm("punāti")
				.citMidForm("punāte")
				.build());

		sktVerbMap.put("pūjayati (√pūj)", new SktVerb.Builder(209, "pūj", 10, "honour")
				.citActForm("pūjayati")
				.citMidForm("pūjayate")
				.build());

		sktVerbMap.put("piparti (√pṛ)", new SktVerb.Builder(210, "pṛ", 3, "fill")
				.addRootClass(9)
				.altRootName("pṝ")
				.citActForm("piparti")
				.build());

		sktVerbMap.put("pṛṇoti (√pṛ)", new SktVerb.Builder(211, "pṛ", 5, "be busy")
				.addRootClass(6)
				.citActForm("pṛṇoti")
				.build());

		sktVerbMap.put("pṛṇakti (√pṛc)", new SktVerb.Builder(212, "pṛc", 7, "mix")
				.citActForm("pṛṇakti")
				.presSub(Pada.ACT, "ṇa")
				.build());

		sktVerbMap.put("pyāyate (√pyāy)", new SktVerb.Builder(213, "pyāy", 1, "overflow")
				.citMidForm("pyāyate")
				.midNormal()
				.build());

		sktVerbMap.put("pṛcchati (√prach)", new SktVerb.Builder(214, "prach", 6, "ask")
				.citActForm("pṛcchati")
				.citMidForm("pṛcchate")
				.build());

		sktVerbMap.put("prathate (√prath)", new SktVerb.Builder(215, "prath", 1, "proclaim")
				.citMidForm("prathate")
				.midNormal()
				.build());

		sktVerbMap.put("prīṇāti (√prī)", new SktVerb.Builder(216, "prī", 9, "delight")
				.citActForm("prīṇāti")
				.citMidForm("prīṇāte")
				.build());

		sktVerbMap.put("plavate (√plu)", new SktVerb.Builder(217, "plu", 1, "drench")
				.citMidForm("plavate")
				.midNormal()
				.build());

		sktVerbMap.put("phalati (√phal)", new SktVerb.Builder(218, "phal", 1, "bear fruit")
				.citActForm("phalati")
				.citMidForm("phalate")
				.build());

		sktVerbMap.put("baṃhate (√baṃh)", new SktVerb.Builder(219, "baṃh", 1, "be strong")
				.citMidForm("baṃhate")
				.midNormal()
				.build());

		sktVerbMap.put("badhnāti (√bandh)", new SktVerb.Builder(220, "bandh", 9, "bind")
				.citActForm("badhnāti")
				.build());

		sktVerbMap.put("bādhate (√bādh)", new SktVerb.Builder(221, "bādh", 1, "oppress")
				.citMidForm("bādhate")
				.citActForm("bādhati")
				.midNormal()
				.build());

		sktVerbMap.put("bodhati (√budh)", new SktVerb.Builder(222, "budh", 1, "waken")
				.addRootClass(4)
				.citActForm("bodhati")
				.citMidForm("bodhate")
				.build());

		sktVerbMap.put("barhati (√bṛh)", new SktVerb.Builder(223, "bṛh", 1, "be great")
				.addRootClass(6)
				.citActForm("barhati")
				.build());

		sktVerbMap.put("bravīti (√brū)", new SktVerb.Builder(224, "brū", 2, "say")
				.citActForm("bravīti")
				.build());

		sktVerbMap.put("bhakṣayati (√bhakṣ)", new SktVerb.Builder(225, "bhakṣ", 10, "eat")
				.citActForm("bhakṣayati")
				.build());

		sktVerbMap.put("bhajati (√bhaj)", new SktVerb.Builder(226, "bhaj", 1, "divide")
				.citActForm("bhajati")
				.citMidForm("bhajate")
				.build());

		sktVerbMap.put("bhanakti (√bhañj)", new SktVerb.Builder(227, "bhañj", 7, "break")
				.citActForm("bhanakti")
				.build());

		sktVerbMap.put("bhāti (√bhā)", new SktVerb.Builder(228, "bhā", 2, "shine")
				.citActForm("bhāti")
				.presPassForm("bhāyate")
				.futForm("bhāsyati")
				.presCausForm("bhāpayati")
				.desForm(Pada.ACT, "bibhāsati")
				.perfForm(Pada.ACT, "babhau")
				.perfParad(Pada.ACT, "dadhau")
				.aorForm(Pada.ACT, "abhāsīt")
				.aorPassForm("abhāyi")
				.aorCausForm("abībhapat")
				.infForm("bhātum")
				.absForm("bhātvā")
				.pppForm("bhāta")
				.completed()
				.build());

		sktVerbMap.put("bhāṣate (√bhāṣ)", new SktVerb.Builder(229, "bhāṣ", 1, "speak")
				.citMidForm("bhāṣate")
				.midNormal()
				.build());

		sktVerbMap.put("bhāsate (√bhās)", new SktVerb.Builder(230, "bhās", 1, "shine")
				.citMidForm("bhāsate")
				.midNormal()
				.build());

		sktVerbMap.put("bhikṣate (√bhikṣ)", new SktVerb.Builder(231, "bhikṣ", 1, "beg")
				.citMidForm("bhikṣate")
				.citActForm("bhikṣati")
				.midNormal()
				.build());

		sktVerbMap.put("bhinatti (√bhid)", new SktVerb.Builder(232, "bhid", 7, "split")
				.citActForm("bhinatti")
				.build());

		sktVerbMap.put("bibheti (√bhī)", new SktVerb.Builder(233, "bhī", 3, "fear")
				.citActForm("bibheti")
				.build());

		sktVerbMap.put("bhunakti (√bhuj)", new SktVerb.Builder(234, "bhuj", 7, "enjoy")
				.citActForm("bhunakti")
				.citMidForm("bhunakte")
				.build());

		sktVerbMap.put("bhujati (√bhuj)", new SktVerb.Builder(235, "bhuj", 6, "bend")
				.citActForm("bhujati")
				.build());

		sktVerbMap.put("bhavati (√bhū)", new SktVerb.Builder(236, "bhū", 1, "become")
				.citActForm("bhavati")
				.presPassForm("bhūyate")
				.futForm("bhaviṣyati")
				.presCausForm("bhāvayati")
				.desForm(Pada.ACT, "bubhūṣati")
				.intForm("bubhūyate")
				.perfForm(Pada.ACT, "babhūva")
				.perfParad(Pada.ACT, "jijīva")
				.perfParad(Pada.MID, "jijīve")
				.perfSub(Pada.ACT, "ūv")
				.perfSub(Pada.MID, "ūv")
				.aorForm(Pada.ACT, "abhūt")
				.aorPassForm("abhāvi")
				.aorCausForm("abībhavat")
				.infForm("bhavitum")
				.fppForm("bhavitavya")
				.absForm("bhūtvā")
				.pppForm("bhūta")
				.fppNiyaForm("bhavanīya")
				.fppYaForm("bhāvya")
				.completed()
				.build());

		sktVerbMap.put("bhūṣati (√bhūṣ)", new SktVerb.Builder(237, "bhūṣ", 1, "adorn")
				.citActForm("bhūṣati")
				.build());

		sktVerbMap.put("bibharti (√bhṛ)", new SktVerb.Builder(238, "bhṛ", 3, "bear")
				.addRootClass(1)
				.citActForm("bibharti")
				.citMidForm("bibharte")
				.build());

		sktVerbMap.put("bhraśyati (√bhraṃś)", new SktVerb.Builder(239, "bhraṃś", 1, "fall")
				.citActForm("bhraśyati")
				.build());

		sktVerbMap.put("bhramati (√bhram)", new SktVerb.Builder(240, "bhram", 1, "wander")
				.addRootClass(4)
				.citActForm("bhramati")
				.citMidForm("bhramate")
				.build());

		sktVerbMap.put("bhṛjjati (√bhrasj)", new SktVerb.Builder(241, "bhrasj", 6, "roast")
				.citActForm("bhṛjjati")
				.citMidForm("bhṛjjate")
				.build());

		sktVerbMap.put("bhrājate (√bhrāj)", new SktVerb.Builder(242, "bhrāj", 1, "shine")
				.citMidForm("bhrājate")
				.midNormal()
				.build());

		sktVerbMap.put("maṃhate (√maṃh)", new SktVerb.Builder(243, "maṃh", 1, "grow")
				.citMidForm("maṃhate")
				.midNormal()
				.build());

		sktVerbMap.put("mathnāti (√math)", new SktVerb.Builder(244, "math", 9, "stir")
				.addRootClass(1)
				.altRootName("manth")
				.citActForm("mathnāti")
				.citMidForm("mathnāte")
				.build());

		sktVerbMap.put("mādyati (√mad)", new SktVerb.Builder(245, "mad", 4, "rejoice")
				.citActForm("mādyati")
				.build());

		sktVerbMap.put("manyate (√man)", new SktVerb.Builder(246, "man", 4, "think")
				.addRootClass(8)
				.citMidForm("manyate")
				.citActForm("manyati")
				.midNormal()
				.build());

		sktVerbMap.put("mandate (√mand)", new SktVerb.Builder(247, "mand", 1, "gladden")
				.citMidForm("mandate")
				.midNormal()
				.build());

		sktVerbMap.put("majjati (√masj)", new SktVerb.Builder(248, "masj", 6, "sink")
				.citActForm("majjati")
				.citMidForm("majjate")
				.build());

		sktVerbMap.put("mahati (√mah)", new SktVerb.Builder(249, "mah", 1, "rejoice")
				.addRootClass(10)
				.citActForm("mahati")
				.citMidForm("mahate")
				.build());

		sktVerbMap.put("māti (√mā)", new SktVerb.Builder(250, "mā", 2, "measure")
				.addRootClass(3, 4)
				.citActForm("māti")
				.irrMidForm("mimīte", "māyate")
				.build());

		sktVerbMap.put("methati (√mith)", new SktVerb.Builder(251, "mith", 1, "associate")
				.citActForm("methati")
				.citMidForm("methate")
				.build());

		sktVerbMap.put("milati (√mil)", new SktVerb.Builder(252, "mil", 6, "meet")
				.citActForm("milati")
				.citMidForm("milate")
				.build());

		sktVerbMap.put("miṣati (√miṣ)", new SktVerb.Builder(253, "miṣ", 6, "wink")
				.citActForm("miṣati")
				.build());

		sktVerbMap.put("mehati (√mih)", new SktVerb.Builder(254, "mih", 1, "urinate")
				.citActForm("mehati")
				.citMidForm("mehate")
				.build());

		sktVerbMap.put("mīnāti (√mī)", new SktVerb.Builder(255, "mī", 9, "lessen")
				.citActForm("mīnāti")
				.citMidForm("mīnāte")
				.build());

		sktVerbMap.put("mīlati (√mīl)", new SktVerb.Builder(256, "mīl", 1, "wink")
				.citActForm("mīlati")
				.build());

		sktVerbMap.put("muñcati (√muc)", new SktVerb.Builder(257, "muc", 6, "release")
				.citActForm("muñcati")
				.citMidForm("muñcate")
				.build());

		sktVerbMap.put("modate (√mud)", new SktVerb.Builder(258, "mud", 1, "rejoice")
				.citMidForm("modate")
				.midNormal()
				.build());

		sktVerbMap.put("muṣṇāti (√muṣ)", new SktVerb.Builder(259, "muṣ", 9, "steal")
				.citActForm("muṣṇāti")
				.build());

		sktVerbMap.put("muhyati (√muh)", new SktVerb.Builder(260, "muh", 4, "err")
				.citActForm("muhyati")
				.build());

		sktVerbMap.put("mūrcchati (√mūrch)", new SktVerb.Builder(261, "mūrch", 1, "stiffen")
				.citActForm("mūrcchati")
				.build());

		sktVerbMap.put("mriyate (√mṛ)", new SktVerb.Builder(262, "mṛ", 6, "die")
				.citMidForm("mriyate")
				.midNormal()
				.build());

		sktVerbMap.put("mṛgayate (√mṛg)", new SktVerb.Builder(263, "mṛg", 10, "hunt")
				.citMidForm("mṛgayate")
				.midNormal()
				.build());

		sktVerbMap.put("mārṣṭi (√mṛj)", new SktVerb.Builder(264, "mṛj", 2, "rub")
				.citActForm("mārṣṭi")
				.build());

		sktVerbMap.put("mṛdnāti (√mṛd)", new SktVerb.Builder(265, "mṛd", 9, "crush")
				.addRootClass(1)
				.citActForm("mṛdnāti")
				.build());

		sktVerbMap.put("mṛśati (√mṛś)", new SktVerb.Builder(266, "mṛś", 6, "touch")
				.citActForm("mṛśati")
				.build());

		sktVerbMap.put("mṛṣyati (√mṛṣ)", new SktVerb.Builder(267, "mṛṣ", 4, "forget")
				.citActForm("mṛṣyati")
				.citMidForm("mṛṣyate")
				.build());

		sktVerbMap.put("manati (√mnā)", new SktVerb.Builder(268, "mnā", 1, "recall")
				.citActForm("manati")
				.build());

		sktVerbMap.put("mlocati (√mluc)", new SktVerb.Builder(269, "mluc", 1, "go")
				.citActForm("mlocati")
				.build());

		sktVerbMap.put("mlecchati (√mlecch)", new SktVerb.Builder(270, "mlecch", 1, "jabber")
				.citActForm("mlecchati")
				.build());

		sktVerbMap.put("mlāyati (√mlai)", new SktVerb.Builder(271, "mlai", 1, "wither")
				.citActForm("mlāyati")
				.citMidForm("mlāyate")
				.build());

		sktVerbMap.put("yajati (√yaj)", new SktVerb.Builder(272, "yaj", 1, "sacrifice")
				.citActForm("yajati")
				.citMidForm("yajate")
				.build());

		sktVerbMap.put("yatate (√yat)", new SktVerb.Builder(273, "yat", 1, "strive")
				.citMidForm("yatate")
				.citActForm("yatati")
				.midNormal()
				.build());

		sktVerbMap.put("yacchati (√yam)", new SktVerb.Builder(274, "yam", 1, "give")
				.citActForm("yacchati")
				.build());

		sktVerbMap.put("yāti (√yā)", new SktVerb.Builder(275, "yā", 2, "go")
				.citActForm("yāti")
				.build());

		sktVerbMap.put("yācati (√yāc)", new SktVerb.Builder(276, "yāc", 1, "request")
				.citActForm("yācati")
				.citMidForm("yācate")
				.build());

		sktVerbMap.put("yunakti (√yuj)", new SktVerb.Builder(277, "yuj", 7, "join")
				.citActForm("yunakti")
				.presPluForm("yuñjanti")
				.citMidForm("yuṅkte")
				.presPassForm("yujyate")
				.futForm("yokṣyati")
				.presCausForm("yojayati")
				.desForm(Pada.ACT, "yuyukṣati")
				.perfForm(Pada.ACT, "yuyoja")
				.perfParad(Pada.ACT, "viveśa")
				.perfParad(Pada.MID, "viviśe")
				.perfSub(Pada.ACT, "oj", "uj")
				.perfSub(Pada.MID, "uj")
				.aorForm(Pada.ACT, "ayujat", "ayaukṣīt", "ayokṣīt")
				.aorForm(Pada.MID, "ayukta")
				.aorCausForm("ayūyujat")
				.infForm("yoktum")
				.fppForm("yoktavya")
				.absForm("yuktvā")
				.pppForm("yukta")
				.fppNiyaForm("yojanīya")
				.fppYaForm("yogya", "yojya")
				.completed()
				.build());

		sktVerbMap.put("yudhyate (√yudh)", new SktVerb.Builder(278, "yudh", 4, "fight")
				.citMidForm("yudhyate")
				.midNormal()
				.build());

		sktVerbMap.put("yupyati (√yup)", new SktVerb.Builder(279, "yup", 4, "block")
				.citActForm("yupyati")
				.build());

		sktVerbMap.put("raṃhati (√raṃh)", new SktVerb.Builder(280, "raṃh", 1, "hasten")
				.citActForm("raṃhati")
				.build());

		sktVerbMap.put("rakṣati (√rakṣ)", new SktVerb.Builder(281, "rakṣ", 1, "protect")
				.citActForm("rakṣati")
				.build());

		sktVerbMap.put("racayati (√rac)", new SktVerb.Builder(282, "rac", 10, "arrange")
				.citActForm("racayati")
				.build());

		sktVerbMap.put("rajati (√rañj)", new SktVerb.Builder(283, "rañj", 1, "be dyed")
				.addRootClass(4)
				.citActForm("rajati")
				.citMidForm("rajate")
				.altActForm("rajyati")
				.altMidForm("rajyate")
				.build());

		sktVerbMap.put("rabhate (√rabh)", new SktVerb.Builder(284, "rabh", 1, "grasp")
				.citMidForm("rabhate")
				.midNormal()
				.build());

		sktVerbMap.put("ramate (√ram)", new SktVerb.Builder(285, "ram", 1, "enjoy")
				.citMidForm("ramate")
				.citActForm("ramati")
				.midNormal()
				.build());

		sktVerbMap.put("rahati (√rah)", new SktVerb.Builder(286, "rah", 1, "abandon")
				.citActForm("rahati")
				.build());

		sktVerbMap.put("rāti (√rā)", new SktVerb.Builder(287, "rā", 2, "bestow")
				.citActForm("rāti")
				.build());

		sktVerbMap.put("rājati (√rāj)", new SktVerb.Builder(288, "rāj", 1, "shine")
				.citActForm("rājati")
				.citMidForm("rājate")
				.build());

		sktVerbMap.put("rādhnoti (√rādh)", new SktVerb.Builder(289, "rādh", 5, "succeed")
				.citActForm("rādhnoti")
				.build());

		sktVerbMap.put("riṇāti (√rī)", new SktVerb.Builder(290, "rī", 9, "flow")
				.addRootClass(4)
				.citActForm("riṇāti")
				.citMidForm("riṇāte")
				.build());

		sktVerbMap.put("riṇakti (√ric)", new SktVerb.Builder(291, "ric", 7, "leave")
				.citActForm("riṇakti")
				.presSub(Pada.ACT, "ṇa")
				.citMidForm("riṇakte")
				.presSub(Pada.MID, "ṇa")
				.build());

		sktVerbMap.put("reṣati (√riṣ)", new SktVerb.Builder(292, "riṣ", 1, "be hurt")
				.addRootClass(4)
				.citActForm("reṣati")
				.build());

		sktVerbMap.put("rauti (√ru)", new SktVerb.Builder(293, "ru", 2, "cry")
				.citActForm("rauti")
				.build());

		sktVerbMap.put("rocate (√ruc)", new SktVerb.Builder(294, "ruc", 1, "shine")
				.citMidForm("rocate")
				.citActForm("rocati")
				.midNormal()
				.build());

		sktVerbMap.put("rujati (√ruj)", new SktVerb.Builder(295, "ruj", 6, "break")
				.citActForm("rujati")
				.build());

		sktVerbMap.put("roditi (√rud)", new SktVerb.Builder(296, "rud", 2, "weep")
				.citActForm("roditi")
				.build());

		sktVerbMap.put("ruṇaddhi (√rudh)", new SktVerb.Builder(297, "rudh", 7, "obstruct")
				.citActForm("ruṇaddhi")
				.citMidForm("ruṇaddhe")
				.build());

		sktVerbMap.put("roṣati (√ruṣ)", new SktVerb.Builder(298, "ruṣ", 1, "be angry")
				.addRootClass(4)
				.citActForm("roṣati")
				.build());

		sktVerbMap.put("rohati (√ruh)", new SktVerb.Builder(299, "ruh", 1, "grow")
				.citActForm("rohati")
				.build());

		sktVerbMap.put("lagati (√lag)", new SktVerb.Builder(300, "lag", 1, "adhere")
				.citActForm("lagati")
				.build());

		sktVerbMap.put("laṅghati (√laṅgh)", new SktVerb.Builder(301, "laṅgh", 1, "jump")
				.citActForm("laṅghati")
				.citMidForm("laṅghate")
				.build());

		sktVerbMap.put("lajjate (√lajj)", new SktVerb.Builder(302, "lajj", 6, "be ashamed")
				.citMidForm("lajjate")
				.citActForm("lajjati")
				.midNormal()
				.build());

		sktVerbMap.put("lapati (√lap)", new SktVerb.Builder(303, "lap", 1, "chatter")
				.citActForm("lapati")
				.citMidForm("lapate")
				.build());

		sktVerbMap.put("labhate (√labh)", new SktVerb.Builder(304, "labh", 1, "obtain")
				.citMidForm("labhate")
				.midNormal()
				.presPassForm("labhyate")
				.futForm("lapsyate", "labhiṣyate")
				.presCausForm("lambhayati")
				.desForm(Pada.MID, "lipsate")
				.perfForm(Pada.MID, "lebhe")
				.perfParad(Pada.MID, "lebhe")
				.aorForm(Pada.MID, "alabdha")
				.aorCausForm("alalambhat")
				.infForm("labdhum")
				.fppForm("labdhavya")
				.absForm("labdhvā")
				.pppForm("labdha")
				.fppNiyaForm("labhanīya")
				.fppYaForm("labhya")
				.completed()
				.build());

		sktVerbMap.put("lambhate (√lamb)", new SktVerb.Builder(305, "lamb", 1, "hang")
				.citMidForm("lambhate")
				.midNormal()
				.build());

		sktVerbMap.put("lalati (√lal)", new SktVerb.Builder(306, "lal", 1, "play")
				.citActForm("lalati")
				.build());

		sktVerbMap.put("lasati (√las)", new SktVerb.Builder(307, "las", 1, "gleam")
				.citActForm("lasati")
				.build());

		sktVerbMap.put("likhati (√likh)", new SktVerb.Builder(308, "likh", 6, "write")
				.citActForm("likhati")
				.build());

		sktVerbMap.put("limpati (√lip)", new SktVerb.Builder(309, "lip", 6, "smear")
				.citActForm("limpati")
				.citMidForm("limpate")
				.build());

		sktVerbMap.put("liśati (√liś)", new SktVerb.Builder(310, "liś", 6, "tear")
				.addRootClass(4)
				.citActForm("liśati")
				.build());

		sktVerbMap.put("leḍhi (√lih)", new SktVerb.Builder(311, "lih", 2, "lick")
				.citActForm("leḍhi")
				.build());

		sktVerbMap.put("līnāti (√lī)", new SktVerb.Builder(312, "lī", 9, "cling")
				.addRootClass(4)
				.citActForm("līnāti")
				.citMidForm("līnāte")
				.build());

		sktVerbMap.put("loṭati (√luṭ)", new SktVerb.Builder(313, "luṭ", 1, "roll")
				.addRootClass(4)
				.citActForm("loṭati")
				.build());

		sktVerbMap.put("luṇṭhayati (√luṇṭh)", new SktVerb.Builder(314, "luṇṭh", 10, "rob")
				.citActForm("luṇṭhayati")
				.build());

		sktVerbMap.put("lumpati (√lup)", new SktVerb.Builder(315, "lup", 6, "break")
				.citActForm("lumpati")
				.citMidForm("lumpate")
				.build());

		sktVerbMap.put("lubhyati (√lubh)", new SktVerb.Builder(316, "lubh", 4, "desire")
				.addRootClass(1)
				.citActForm("lubhyati")
				.build());

		sktVerbMap.put("lunāti (√lū)", new SktVerb.Builder(317, "lū", 9, "cut off")
				.citActForm("lunāti")
				.citMidForm("lunāte")
				.build());

		sktVerbMap.put("lokate (√lok)", new SktVerb.Builder(318, "lok", 1, "look")
				.citMidForm("lokate")
				.midNormal()
				.build());

		sktVerbMap.put("locayati (√loc)", new SktVerb.Builder(319, "loc", 10, "consider")
				.citActForm("locayati")
				.citMidForm("locayate")
				.build());

		sktVerbMap.put("vakti (√vac)", new SktVerb.Builder(320, "vac", 2, "speak")
				.citActForm("vakti")
				.build());

		sktVerbMap.put("vañcati (√vañc)", new SktVerb.Builder(321, "vañc", 1, "stray")
				.citActForm("vañcati")
				.build());

		sktVerbMap.put("vadati (√vad)", new SktVerb.Builder(322, "vad", 1, "speak")
				.citActForm("vadati")
				.citMidForm("vadate")
				.build());

		sktVerbMap.put("hanti (√vadh)", new SktVerb.Builder(323, "vadh", 1, "kill")
				.citActForm("hanti")
				.build());

		sktVerbMap.put("vanoti (√van)", new SktVerb.Builder(324, "van", 8, "love")
				.citActForm("vanoti")
				.citMidForm("vanote")
				.build());

		sktVerbMap.put("vandate (√vand)", new SktVerb.Builder(325, "vand", 1, "salute")
				.citMidForm("vandate")
				.citActForm("vandati")
				.midNormal()
				.build());

		sktVerbMap.put("vapati (√vap)", new SktVerb.Builder(326, "vap", 1, "sow")
				.citActForm("vapati")
				.citMidForm("vapate")
				.build());

		sktVerbMap.put("varṇayati (√varṇ)", new SktVerb.Builder(327, "varṇ", 10, "depict")
				.citActForm("varṇayati")
				.build());

		sktVerbMap.put("vaṣṭi (√vaś)", new SktVerb.Builder(328, "vaś", 2, "wish")
				.citActForm("vaṣṭi")
				.build());

		sktVerbMap.put("vasati (√vas)", new SktVerb.Builder(329, "vas", 1, "dwell")
				.citActForm("vasati")
				.build());

		sktVerbMap.put("vaste (√vas)", new SktVerb.Builder(330, "vas", 2, "wear")
				.citMidForm("vaste")
				.midNormal()
				.build());

		sktVerbMap.put("vāsayati (√vas)", new SktVerb.Builder(331, "vas", 10, "cut")
				.citActForm("vāsayati")
				.build());

		sktVerbMap.put("vahati (√vah)", new SktVerb.Builder(332, "vah", 1, "carry")
				.citActForm("vahati")
				.citMidForm("vahate")
				.build());

		sktVerbMap.put("vāti (√vā)", new SktVerb.Builder(333, "vā", 2, "blow")
				.citActForm("vāti")
				.build());

		sktVerbMap.put("vāñchati (√vāñch)", new SktVerb.Builder(334, "vāñch", 1, "wish")
				.citActForm("vāñchati")
				.build());

		sktVerbMap.put("vāśyate (√vāś)", new SktVerb.Builder(335, "vāś", 4, "bleat")
				.citMidForm("vāśyate")
				.citActForm("vāśyati")
				.midNormal()
				.build());

		sktVerbMap.put("vinakti (√vic)", new SktVerb.Builder(336, "vic", 7, "separate")
				.citActForm("vinakti")
				.citMidForm("vinakte")
				.build());

		sktVerbMap.put("vijate (√vij)", new SktVerb.Builder(337, "vij", 6, "quiver")
				.citMidForm("vijate")
				.midNormal()
				.build());

		sktVerbMap.put("vetti (√vid)", new SktVerb.Builder(338, "vid", 2, "know")
				.citActForm("vetti")
				.build());

		sktVerbMap.put("vindati (√vid)", new SktVerb.Builder(339, "vid", 6, "find")
				.citActForm("vindati")
				.citMidForm("vindate")
				.build());

		sktVerbMap.put("viśati (√viś)", new SktVerb.Builder(340, "viś", 6, "enter")
				.citActForm("viśati")
				.build());

		sktVerbMap.put("veti (√vī)", new SktVerb.Builder(341, "vī", 2, "enjoy")
				.citActForm("veti")
				.build());

		sktVerbMap.put("vṛṇoti (√vṛ)", new SktVerb.Builder(342, "vṛ", 5, "cover")
				.addRootClass(9, 1)
				.citActForm("vṛṇoti")
				.citMidForm("vṛṇote")
				.build());

		sktVerbMap.put("vṛṇakti (√vṛj)", new SktVerb.Builder(343, "vṛj", 7, "twist")
				.addRootClass(1)
				.citActForm("vṛṇakti")
				.presSub(Pada.ACT, "ṇa")
				.build());

		sktVerbMap.put("vartate (√vṛt)", new SktVerb.Builder(344, "vṛt", 1, "turn")
				.citMidForm("vartate")
				.midNormal()
				.build());

		sktVerbMap.put("vardhate (√vṛdh)", new SktVerb.Builder(345, "vṛdh", 1, "grow")
				.citMidForm("vardhate")
				.citActForm("vardhati")
				.midNormal()
				.build());

		sktVerbMap.put("varṣati (√vṛṣ)", new SktVerb.Builder(346, "vṛṣ", 1, "rain")
				.citActForm("varṣati")
				.citMidForm("varṣate")
				.build());

		sktVerbMap.put("vṛhati (√vṛh)", new SktVerb.Builder(347, "vṛh", 6, "tear")
				.citActForm("vṛhati")
				.build());

		sktVerbMap.put("vayati (√ve)", new SktVerb.Builder(348, "ve", 1, "weave")
				.citActForm("vayati")
				.citMidForm("vayate")
				.build());

		sktVerbMap.put("vepate (√vep)", new SktVerb.Builder(349, "vep", 1, "tremble")
				.citMidForm("vepate")
				.citActForm("vepati")
				.midNormal()
				.build());

		sktVerbMap.put("vicati (√vyac)", new SktVerb.Builder(350, "vyac", 6, "embrace")
				.citActForm("vicati")
				.build());

		sktVerbMap.put("vyathate (√vyath)", new SktVerb.Builder(351, "vyath", 1, "suffer")
				.citMidForm("vyathate")
				.citActForm("vyathati")
				.midNormal()
				.build());

		sktVerbMap.put("vidhyati (√vyadh)", new SktVerb.Builder(352, "vyadh", 4, "pierce")
				.citActForm("vidhyati")
				.build());

		sktVerbMap.put("vrajati (√vraj)", new SktVerb.Builder(353, "vraj", 1, "proceed")
				.citActForm("vrajati")
				.build());

		sktVerbMap.put("vṛścati (√vraśc)", new SktVerb.Builder(354, "vraśc", 6, "hew")
				.citActForm("vṛścati")
				.build());

		sktVerbMap.put("śaṃsati (√śaṃs)", new SktVerb.Builder(355, "śaṃs", 1, "praise")
				.citActForm("śaṃsati")
				.citMidForm("śaṃsate")
				.build());

		sktVerbMap.put("śaknoti (√śak)", new SktVerb.Builder(356, "śak", 5, "be able")
				.addRootClass(4)
				.citActForm("śaknoti")
				.build());

		sktVerbMap.put("śaṅkate (√śaṅk)", new SktVerb.Builder(357, "śaṅk", 1, "hesitate")
				.citMidForm("śaṅkate")
				.midNormal()
				.build());

		sktVerbMap.put("śapati (√śap)", new SktVerb.Builder(358, "śap", 1, "curse")
				.addRootClass(4)
				.citActForm("śapati")
				.citMidForm("śapate")
				.altActForm("śapyati")
				.altMidForm("śapyate")
				.build());

		sktVerbMap.put("śāmyati (√śam)", new SktVerb.Builder(359, "śam", 4, "be quiet")
				.addRootClass(1)
				.citActForm("śāmyati")
				.build());

		sktVerbMap.put("śāmayate (√śam)", new SktVerb.Builder(360, "śam", 10, "observe")
				.citMidForm("śāmayate")
				.midNormal()
				.build());

		sktVerbMap.put("śāsti (√śās)", new SktVerb.Builder(361, "śās", 2, "instruct")
				.citActForm("śāsti")
				.build());

		sktVerbMap.put("śikṣate (√śikṣ)", new SktVerb.Builder(362, "śikṣ", 1, "learn")
				.citMidForm("śikṣate")
				.midNormal()
				.build());

		sktVerbMap.put("śinaṣṭi (√śiṣ)", new SktVerb.Builder(363, "śiṣ", 7, "remain")
				.addRootClass(1)
				.citActForm("śinaṣṭi")
				.build());

		sktVerbMap.put("śete (√śī)", new SktVerb.Builder(364, "śī", 2, "sleep")
				.citMidForm("śete")
				.midNormal()
				.build());

		sktVerbMap.put("śocati (√śuc)", new SktVerb.Builder(365, "śuc", 1, "grieve")
				.citActForm("śocati")
				.citMidForm("śocate")
				.build());

		sktVerbMap.put("śudhyati (√śudh)", new SktVerb.Builder(366, "śudh", 4, "be pure")
				.citActForm("śudhyati")
				.build());

		sktVerbMap.put("śobhati (√śubh)", new SktVerb.Builder(367, "śubh", 1, "shine")
				.addRootClass(6)
				.citActForm("śobhati")
				.citMidForm("śobhate")
				.build());

		sktVerbMap.put("śuṣyati (√śuṣ)", new SktVerb.Builder(368, "śuṣ", 4, "dry up")
				.citActForm("śuṣyati")
				.build());

		sktVerbMap.put("śṛṇāti (√śṝ)", new SktVerb.Builder(369, "śṝ", 9, "crush")
				.citActForm("śṛṇāti")
				.build());

		sktVerbMap.put("ścotati (√ścut)", new SktVerb.Builder(370, "ścut", 1, "drip")
				.citActForm("ścotati")
				.build());

		sktVerbMap.put("śyāyati (√śyai)", new SktVerb.Builder(371, "śyai", 1, "congeal")
				.citActForm("śyāyati")
				.citMidForm("śyāyate")
				.build());

		sktVerbMap.put("śrathati (√śrath)", new SktVerb.Builder(372, "śrath", 1, "get loose")
				.addRootClass(9)
				.citActForm("śrathati")
				.citMidForm("śrathate")
				.build());

		sktVerbMap.put("śrāmyati (√śram)", new SktVerb.Builder(373, "śram", 4, "weary")
				.citActForm("śrāmyati")
				.build());

		sktVerbMap.put("śrambhate (√śrambh)", new SktVerb.Builder(374, "śrambh", 1, "err")
				.citMidForm("śrambhate")
				.midNormal()
				.build());

		sktVerbMap.put("śrāti (√śrā)", new SktVerb.Builder(375, "śrā", 2, "cook")
				.citActForm("śrāti")
				.altActForm("śrāyati")
				.build());

		sktVerbMap.put("śrayati (√śri)", new SktVerb.Builder(376, "śri", 1, "take refuge")
				.citActForm("śrayati")
				.citMidForm("śrayate")
				.build());

		sktVerbMap.put("śṛṇoti (√śru)", new SktVerb.Builder(377, "śru", 5, "hear")
				.citActForm("śṛṇoti")
				.citMidForm("śṛṇote")
				.build());

		sktVerbMap.put("ślāghate (√ślāgh)", new SktVerb.Builder(378, "ślāgh", 1, "confide")
				.citMidForm("ślāghate")
				.midNormal()
				.build());

		sktVerbMap.put("śvasiti (√śvas)", new SktVerb.Builder(379, "śvas", 2, "breathe")
				.addRootClass(1)
				.citActForm("śvasiti")
				.build());

		sktVerbMap.put("śvayati (√śvi)", new SktVerb.Builder(380, "śvi", 1, "swell")
				.citActForm("śvayati")
				.build());

		sktVerbMap.put("sajati (√sañj)", new SktVerb.Builder(381, "sañj", 1, "adhere")
				.citActForm("sajati")
				.altActForm("sañjati")
				.build());

		sktVerbMap.put("sādati (√sad)", new SktVerb.Builder(382, "sad", 1, "sit")
				.citActForm("sādati")
				.citMidForm("sādate")
				.build());

		sktVerbMap.put("sahate (√sah)", new SktVerb.Builder(383, "sah", 1, "endure")
				.citMidForm("sahate")
				.citActForm("sahati")
				.midNormal()
				.build());

		sktVerbMap.put("sādhnoti (√sādh)", new SktVerb.Builder(384, "sādh", 5, "accomplish")
				.addRootClass(1)
				.citActForm("sādhnoti")
				.build());

		sktVerbMap.put("sinoti (√si)", new SktVerb.Builder(385, "si", 5, "bind")
				.addRootClass(9)
				.citActForm("sinoti")
				.citMidForm("sinote")
				.build());

		sktVerbMap.put("siñcati (√sic)", new SktVerb.Builder(386, "sic", 6, "sprinkle")
				.citActForm("siñcati")
				.citMidForm("siñcate")
				.build());

		sktVerbMap.put("sedhati (√sidh)", new SktVerb.Builder(387, "sidh", 1, "repel")
				.citActForm("sedhati")
				.build());

		sktVerbMap.put("sidhyati (√sidh)", new SktVerb.Builder(388, "sidh", 4, "succeed")
				.citActForm("sidhyati")
				.citMidForm("sidhyate")
				.build());

		sktVerbMap.put("sīvyati (√siv)", new SktVerb.Builder(389, "siv", 4, "sew")
				.citActForm("sīvyati")
				.build());

		sktVerbMap.put("sunoti (√su)", new SktVerb.Builder(390, "su", 5, "press")
				.citActForm("sunoti")
				.citMidForm("sunute")
				.presPassForm("sūyate")
				.futForm("soṣyati", "sviṣyati")
				.presCausForm("sāvayati")
				.desForm(Pada.ACT, "susūṣati")
				.desForm(Pada.MID, "susūṣate")
				.perfForm(Pada.ACT, "suṣāva")
				.perfParad(Pada.ACT, "śiśrāya")
				.perfParad(Pada.MID, "śiśriye")
				.perfSub(Pada.ACT, "āv", "av", "iv")
				.perfSub(Pada.MID, "iv")
				.aorForm(Pada.ACT, "asauṣīt", "asāvīt")
				.aorForm(Pada.MID, "asoṣṭa", "asāviṣṭa")
				.aorCausForm("asūṣavat")
				.infForm("sotum")
				.absForm("sotvā")
				.pppForm("sota")
				.completed()
				.build());

		sktVerbMap.put("sūdate (√sūd)", new SktVerb.Builder(391, "sūd", 1, "achieve")
				.citMidForm("sūdate")
				.midNormal()
				.build());

		sktVerbMap.put("sarati (√sṛ)", new SktVerb.Builder(392, "sṛ", 1, "flow")
				.citActForm("sarati")
				.citMidForm("sarate")
				.build());

		sktVerbMap.put("sṛjati (√sṛj)", new SktVerb.Builder(393, "sṛj", 6, "emit")
				.addRootClass(4)
				.citActForm("sṛjati")
				.build());

		sktVerbMap.put("sarpati (√sṛp)", new SktVerb.Builder(394, "sṛp", 1, "creep")
				.citActForm("sarpati")
				.citMidForm("sarpate")
				.build());

		sktVerbMap.put("sevate (√sev)", new SktVerb.Builder(395, "sev", 1, "serve")
				.citMidForm("sevate")
				.midNormal()
				.build());

		sktVerbMap.put("skandati (√skand)", new SktVerb.Builder(396, "skand", 1, "dart")
				.citActForm("skandati")
				.build());

		sktVerbMap.put("stabhnāti (√stambh)", new SktVerb.Builder(397, "stambh", 9, "uphold")
				.citActForm("stabhnāti")
				.build());

		sktVerbMap.put("stauti (√stu)", new SktVerb.Builder(398, "stu", 2, "praise")
				.citActForm("stauti")
				.citMidForm("stute")
				.presPassForm("stūyate")
				.futForm("stoṣyati")
				.presCausForm("stāvayati")
				.desForm(Pada.ACT, "tuṣṭūṣati")
				.perfForm(Pada.ACT, "tuṣṭāva")
				.perfParad(Pada.ACT, "tuṣṭāva")
				.perfParad(Pada.MID, "tuṣṭuve")
				.aorForm(Pada.ACT, "astauṣīt", "astāvīt")
				.aorForm(Pada.MID, "astoṣṭa")
				.aorCausForm("atuṣṭavat")
				.infForm("stotum")
				.absForm("stutvā")
				.pppForm("stuta")
				.fppNiyaForm("stavanīya")
				.fppYaForm("stavya")
				.completed()
				.build());

		sktVerbMap.put("stṛṇoti (√stṛ)", new SktVerb.Builder(399, "stṛ", 5, "overthrow")
				.addRootClass(9)
				.citActForm("stṛṇoti")
				.citMidForm("stṛṇote")
				.build());

		sktVerbMap.put("tiṣṭati (√sthā)", new SktVerb.Builder(400, "sthā", 1, "stand")
				.citActForm("tiṣṭati")
				.citMidForm("tiṣṭate")
				.build());

		sktVerbMap.put("snāti (√snā)", new SktVerb.Builder(401, "snā", 2, "bathe")
				.citActForm("snāti")
				.build());

		sktVerbMap.put("snihyati (√snih)", new SktVerb.Builder(402, "snih", 4, "love")
				.citActForm("snihyati")
				.build());

		sktVerbMap.put("spardhate (√spardh)", new SktVerb.Builder(403, "spardh", 1, "strive")
				.citMidForm("spardhate")
				.midNormal()
				.build());

		sktVerbMap.put("spṛśati (√spṛś)", new SktVerb.Builder(404, "spṛś", 6, "touch")
				.citActForm("spṛśati")
				.citMidForm("spṛśate")
				.build());

		sktVerbMap.put("spṛhayati (√spṛh)", new SktVerb.Builder(405, "spṛh", 10, "desire")
				.citActForm("spṛhayati")
				.build());

		sktVerbMap.put("sphurati (√sphur)", new SktVerb.Builder(406, "sphur", 6, "dart")
				.citActForm("sphurati")
				.build());

		sktVerbMap.put("smayate (√smi)", new SktVerb.Builder(407, "smi", 1, "smile")
				.citMidForm("smayate")
				.midNormal()
				.build());

		sktVerbMap.put("smarati (√smṛ)", new SktVerb.Builder(408, "smṛ", 1, "remember")
				.citActForm("smarati")
				.build());

		sktVerbMap.put("syandate (√syand)", new SktVerb.Builder(409, "syand", 1, "flow")
				.citMidForm("syandate")
				.midNormal()
				.build());

		sktVerbMap.put("sravati (√sru)", new SktVerb.Builder(410, "sru", 1, "flow")
				.citActForm("sravati")
				.build());

		sktVerbMap.put("svajate (√svañj)", new SktVerb.Builder(411, "svañj", 1, "embrace")
				.citMidForm("svajate")
				.midNormal()
				.build());

		sktVerbMap.put("svādate (√svad)", new SktVerb.Builder(412, "svad", 1, "relish")
				.citMidForm("svādate")
				.citActForm("svādati")
				.midNormal()
				.build());

		sktVerbMap.put("svanati (√svan)", new SktVerb.Builder(413, "svan", 1, "resound")
				.citActForm("svanati")
				.build());

		sktVerbMap.put("svapiti (√svap)", new SktVerb.Builder(414, "svap", 2, "sleep")
				.citActForm("svapiti")
				.presPassForm("supyate")
				.futForm("svapsyati")
				.presCausForm("svāpayati")
				.desForm(Pada.ACT, "suṣupsati")
				.perfForm(Pada.ACT, "suṣvāya")
				.perfParad(Pada.ACT, "śiśrāya")
				.perfParad(Pada.MID, "śiśriye")
				.aorForm(Pada.ACT, "asvāpsīt")
				.aorPassForm("asvāpi")
				.aorCausForm("asiṣvapat")
				.infForm("svaptum")
				.fppForm("svaptavya")
				.absForm("suptvā")
				.pppForm("supta")
				.completed()
				.build());

		sktVerbMap.put("svidyati (√svid)", new SktVerb.Builder(415, "svid", 4, "sweat")
				.addRootClass(1)
				.citActForm("svidyati")
				.build());

		sktVerbMap.put("svarati (√svṛ)", new SktVerb.Builder(416, "svṛ", 1, "sound")
				.citActForm("svarati")
				.build());

		sktVerbMap.put("hanti (√han)", new SktVerb.Builder(417, "han", 2, "kill")
				.citActForm("hanti")
				.build());

		sktVerbMap.put("haryati (√hary)", new SktVerb.Builder(418, "hary", 1, "enjoy")
				.citActForm("haryati")
				.citMidForm("haryate")
				.build());

		sktVerbMap.put("hasati (√has)", new SktVerb.Builder(419, "has", 1, "laugh")
				.citActForm("hasati")
				.citMidForm("hasate")
				.build());

		sktVerbMap.put("jahāti (√hā)", new SktVerb.Builder(420, "hā", 3, "abandon")
				.citActForm("jahāti")
				.build());

		sktVerbMap.put("jihīte (√hā)", new SktVerb.Builder(421, "hā", 3, "go forth")
				.citMidForm("jihīte")
				.midNormal()
				.build());

		sktVerbMap.put("hinoti (√hi)", new SktVerb.Builder(422, "hi", 5, "impel")
				.citActForm("hinoti")
				.citMidForm("hinote")
				.build());

		sktVerbMap.put("hiṃsati (√hiṃs)", new SktVerb.Builder(423, "hiṃs", 1, "injure")
				.addRootClass(7)
				.citActForm("hiṃsati")
				.build());

		sktVerbMap.put("juhoti (√hu)", new SktVerb.Builder(424, "hu", 3, "sacrifice")
				.citActForm("juhoti")
				.citMidForm("juhute")
				.presPassForm("hūyate")
				.futForm("hoṣyati")
				.presCausForm("hāvayati")
				.desForm(Pada.ACT, "juhūṣati")
				.perfForm(Pada.ACT, "juhāva")
				.perfParad(Pada.ACT, "śiśrāya")
				.perfParad(Pada.MID, "śiśriye")
				.perfSub(Pada.ACT, "āv", "av", "iv")
				.perfSub(Pada.MID, "iv")
				.perfPeriForm(Pada.ACT, "juhavām āsa")
				.aorForm(Pada.ACT, "ahauṣīt")
				.aorPassForm("ahāvi")
				.aorCausForm("ajūhayat")
				.infForm("hotum")
				.fppForm("hotavya")
				.absForm("hutvā")
				.pppForm("huta")
				.fppYaForm("havya")
				.completed()
				.build());

		sktVerbMap.put("harati (√hṛ)", new SktVerb.Builder(425, "hṛ", 1, "take")
				.citActForm("harati")
				.citMidForm("harate")
				.build());

		sktVerbMap.put("hṛṣyati (√hṛṣ)", new SktVerb.Builder(426, "hṛṣ", 4, "rejoice")
				.citActForm("hṛṣyati")
				.citMidForm("hṛṣyate")
				.build());

		sktVerbMap.put("hrasati (√hras)", new SktVerb.Builder(427, "hras", 1, "diminish")
				.citActForm("hrasati")
				.citMidForm("hrasate")
				.build());

		sktVerbMap.put("hrādate (√hrād)", new SktVerb.Builder(428, "hrād", 1, "rattle")
				.citMidForm("hrādate")
				.midNormal()
				.build());

		sktVerbMap.put("jihreti (√hrī)", new SktVerb.Builder(429, "hrī", 3, "blush")
				.citActForm("jihreti")
				.build());

		sktVerbMap.put("hlādate (√hlād)", new SktVerb.Builder(430, "hlād", 1, "refresh")
				.citMidForm("hlādate")
				.midNormal()
				.build());

		sktVerbMap.put("hvarati (√hvṛ)", new SktVerb.Builder(431, "hvṛ", 1, "bend")
				.citActForm("hvarati")
				.build());

		sktVerbMap.put("hvayati (√hve)", new SktVerb.Builder(432, "hve", 1, "call")
				.citActForm("hvayati")
				.citMidForm("hvayate")
				.build());

		sktVerbMap.put("adhīte (adhi + √i)", new SktVerb.Builder(16, "i", 1, "study")
				.subRef("a")
				.prefix("adhi")
				.citMidForm("adhīte")
				.midNormal()
				.build());

	}

}

