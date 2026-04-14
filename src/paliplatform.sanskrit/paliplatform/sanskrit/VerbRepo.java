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
 * @version 4.1
 * @since 4.1
 */
public final class VerbRepo {
	public static Map<String, SktVerb> sktVerbMap = new LinkedHashMap<>();

	private VerbRepo() {
	}
	
	// static box to initialize all verbs
	static {

		sktVerbMap.put("añcati", new SktVerb.Builder(1, "añc", 1, "bend")
				.citActForm("añcati")
				.build());

		sktVerbMap.put("anakti", new SktVerb.Builder(2, "añj", 7, "anoint")
				.citActForm("anakti")
				.citMidForm("anakte")
				.build());

		sktVerbMap.put("aṭati", new SktVerb.Builder(3, "aṭ", 1, "wander")
				.citActForm("aṭati")
				.citMidForm("aṭate")
				.build());

		sktVerbMap.put("atti", new SktVerb.Builder(4, "ad", 2, "eat")
				.citActForm("atti")
				.build());

		sktVerbMap.put("aniti", new SktVerb.Builder(5, "an", 2, "breath")
				.citActForm("aniti")
				.build());

		sktVerbMap.put("arthayate", new SktVerb.Builder(6, "arth", 10, "ask for")
				.citMidForm("arthayate")
				.build());

		sktVerbMap.put("arhati", new SktVerb.Builder(7, "arh", 1, "deserve")
				.citActForm("arhati")
				.build());

		sktVerbMap.put("avati", new SktVerb.Builder(8, "av", 1, "further")
				.citActForm("avati")
				.build());

		sktVerbMap.put("aśnute", new SktVerb.Builder(9, "aś", 5, "obtain")
				.citMidForm("aśnute")
				.build());

		sktVerbMap.put("aśnāti", new SktVerb.Builder(10, "aś", 9, "eat")
				.citActForm("aśnāti")
				.build());

		sktVerbMap.put("asti", new SktVerb.Builder(11, "as", 2, "be")
				.citActForm("asti")
				.perfForm(Pada.ACT, "āsa")
				.perfParad(Pada.ACT, "āsa")
				.build());

		sktVerbMap.put("asyati", new SktVerb.Builder(12, "as", 4, "throw")
				.citActForm("asyati")
				.build());

		sktVerbMap.put("āha", new SktVerb.Builder(13, "ah", 1, "say")
				.citActForm("āha")
				.perfForm(Pada.ACT, "āha")
				.perfParad(Pada.ACT, "āha")
				.build());

		sktVerbMap.put("āpnoti", new SktVerb.Builder(14, "āp", 5, "acquire")
				.citActForm("āpnoti")
				.build());

		sktVerbMap.put("āste", new SktVerb.Builder(15, "ās", 2, "sit")
				.citMidForm("āste")
				.build());

		sktVerbMap.put("eti", new SktVerb.Builder(16, "i", 2, "go")
				.addRootClass(1)
				.citActForm("eti")
				.build());

		sktVerbMap.put("inddhe", new SktVerb.Builder(17, "indh", 7, "kindle")
				.citMidForm("inddhe")
				.build());

		sktVerbMap.put("icchati", new SktVerb.Builder(18, "iṣ", 6, "desire")
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

		sktVerbMap.put("iṣyati", new SktVerb.Builder(19, "iṣ", 4, "send")
				.citActForm("iṣyati")
				.build());

		sktVerbMap.put("īkṣate", new SktVerb.Builder(20, "īkṣ", 1, "see")
				.citMidForm("īkṣate")
				.build());

		sktVerbMap.put("īṭṭe", new SktVerb.Builder(21, "īḍ", 2, "praise")
				.citMidForm("īṭṭe")
				.build());

		sktVerbMap.put("īrte", new SktVerb.Builder(22, "īr", 2, "move")
				.citMidForm("īrte")
				.build());

		sktVerbMap.put("īṣṭe", new SktVerb.Builder(23, "īś", 2, "rule")
				.citMidForm("īṣṭe")
				.build());

		sktVerbMap.put("īṣaṭe", new SktVerb.Builder(24, "īṣ", 1, "flee")
				.citMidForm("īṣaṭe")
				.build());

		sktVerbMap.put("karoti", new SktVerb.Builder(45, "kṛ", 8, "do")
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

		sktVerbMap.put("gacchati", new SktVerb.Builder(72, "gam", 1, "go")
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

		sktVerbMap.put("jāyate", new SktVerb.Builder(107, "jan", 4, "be born")
				.citMidForm("jāyate")
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

		sktVerbMap.put("jānāti", new SktVerb.Builder(115, "jñā", 9, "know")
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

		sktVerbMap.put("nayati", new SktVerb.Builder(189, "nī", 1, "lead")
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

		sktVerbMap.put("pacati", new SktVerb.Builder(193, "pac", 1, "cook")
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

		sktVerbMap.put("bhāti", new SktVerb.Builder(228, "bhā", 2, "shine")
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

		sktVerbMap.put("bhavati", new SktVerb.Builder(236, "bhū", 1, "become")
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

		sktVerbMap.put("yunakti", new SktVerb.Builder(277, "yuj", 7, "join")
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

		sktVerbMap.put("labhate", new SktVerb.Builder(304, "labh", 1, "obtain")
				.citMidForm("labhate")
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

		sktVerbMap.put("sunoti", new SktVerb.Builder(390, "su", 5, "press")
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

		sktVerbMap.put("stauti", new SktVerb.Builder(398, "stu", 2, "praise")
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

		sktVerbMap.put("svapiti", new SktVerb.Builder(414, "svap", 2, "sleep")
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

		sktVerbMap.put("juhoti", new SktVerb.Builder(424, "hu", 3, "sacrifice")
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

	}

}

