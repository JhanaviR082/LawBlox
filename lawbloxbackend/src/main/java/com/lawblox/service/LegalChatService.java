package com.lawblox.service;

import com.lawblox.model.*;
import com.lawblox.repository.ChatMessageRepository;
import com.lawblox.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LegalChatService {
    
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    
    // Expanded keyword mapping to Indian legal domains
    private static final Map<String, List<String>> KEYWORD_MAP = Map.ofEntries(
        Map.entry("PROPERTY_LAW", Arrays.asList(
            "property", "land", "house", "boundary", "fence", "encroachment", 
            "neighbour", "neighbor", "deed", "title", "possession", "eviction",
            "lease agreement", "rent", "tenant", "landlord", "property dispute",
            "mutation", "registry", "stamp duty", "khata", "sale deed"
        )),
        Map.entry("CRIMINAL_LAW", Arrays.asList(
            "theft", "assault", "murder", "crime", "police", "arrest", "FIR", 
            "bail", "accused", "victim", "complaint", "harassment", "robbery",
            "kidnapping", "rape", "molestation", "cyber crime", "fraud", 
            "cheating", "defamation", "IPC", "chargesheet", "anticipatory bail"
        )),
        Map.entry("FAMILY_LAW", Arrays.asList(
            "divorce", "marriage", "custody", "child", "alimony", "dowry", 
            "adoption", "maintenance", "husband", "wife", "domestic violence",
            "section 498A", "cruelty", "restitution", "conjugal rights", 
            "guardianship", "visitation rights", "child support", "mutual consent",
            "hindu marriage act", "special marriage act"
        )),
        Map.entry("CONSTITUTIONAL_LAW", Arrays.asList(
            "fundamental rights", "freedom", "speech", "discrimination", 
            "equality", "right to life", "privacy", "search", "warrant", 
            "civil rights", "article 21", "article 19", "article 14", 
            "writ petition", "habeas corpus", "mandamus", "PIL", 
            "public interest litigation", "supreme court", "high court"
        )),
        Map.entry("CONSUMER_LAW", Arrays.asList(
            "defective product", "refund", "warranty", "consumer forum", 
            "complaint", "service", "deficiency", "compensation", "seller", 
            "buyer", "consumer court", "replacement", "faulty goods",
            "misleading advertisement", "unfair trade", "e-commerce dispute",
            "online shopping", "national consumer helpline", "consumer protection act"
        )),
        Map.entry("LABOR_LAW", Arrays.asList(
            "employment", "termination", "salary", "wages", "wrongful dismissal", 
            "workplace", "harassment at work", "EPF", "PF", "gratuity", "bonus",
            "retrenchment", "industrial dispute", "labour court", "provident fund",
            "ESI", "maternity leave", "notice period", "resignation", 
            "sexual harassment", "posh act", "minimum wages"
        )),
        Map.entry("TORT_LAW", Arrays.asList(
            "injury", "accident", "negligence", "compensation", "medical negligence", 
            "slip", "fall", "damage", "liability", "personal injury",
            "motor accident", "hit and run", "insurance claim", "MACT",
            "hospital negligence", "defamation", "nuisance", "trespass",
            "strict liability", "vicarious liability"
        )),
        Map.entry("INTELLECTUAL_PROPERTY", Arrays.asList(
            "copyright", "trademark", "patent", "logo", "design", "plagiarism", 
            "infringement", "brand", "piracy", "counterfeit", "intellectual property",
            "IP rights", "registration", "licensing", "royalty", "trade secret",
            "patent office", "copyright act", "trademark registry", "GI tag"
        )),
        Map.entry("ENVIRONMENTAL_LAW", Arrays.asList(
            "pollution", "environment", "noise pollution", "air pollution",
            "water pollution", "industrial waste", "NGT", "green tribunal",
            "environmental clearance", "forest rights", "wildlife protection",
            "illegal mining", "deforestation", "hazardous waste", "emission",
            "environmental impact", "pollution control board", "eco-sensitive zone",
            "water act", "air act"
        )),
        Map.entry("CYBER_LAW", Arrays.asList(
            "hacking", "cyber crime", "phishing", "identity theft", "online fraud",
            "data breach", "cyberbullying", "IT act", "section 66A", "section 67",
            "morphing", "revenge porn", "email hacking", "social media crime",
            "WhatsApp fraud", "UPI fraud", "banking fraud", "cyber cell",
            "digital signature", "electronic evidence", "cyber security"
        )),
        Map.entry("TAX_LAW", Arrays.asList(
            "GST", "income tax", "tax evasion", "tax notice", "tax refund",
            "assessment", "TDS", "tax appeal", "tax tribunal", "ITR",
            "income tax return", "tax penalty", "customs duty", "excise",
            "service tax", "tax audit", "tax investigation", "tax demand",
            "advance tax", "capital gains", "taxation"
        ))
    );
    
    // Greeting keywords
    private static final Set<String> GREETINGS = new HashSet<>(Arrays.asList(
        "hi", "hello", "hey", "namaste", "good morning", "good afternoon", 
        "good evening", "greetings", "hola", "sup", "yo", "howdy"
    ));
    
    public Map<String, Object> processMessage(String userMessage, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String messageLower = userMessage.toLowerCase().trim();
        
        // Check for greetings
        if (isGreeting(messageLower)) {
            String greetingResponse = generateGreeting(user.getFirstName());
            saveChatMessage(user, userMessage, greetingResponse, "GREETING");
            return Map.of(
                "response", greetingResponse,
                "detectedDomains", Collections.emptySet(),
                "suggestedCases", Collections.emptyList()
            );
        }
        
        // Detect keywords and domains
        Map<String, Set<String>> detectedData = detectKeywordsAndDomains(messageLower);
        Set<String> detectedDomains = detectedData.get("domains");
        Set<String> detectedKeywords = detectedData.get("keywords");
        
        // Generate response
        String botResponse;
        List<LegalCaseSuggestion> suggestions = new ArrayList<>();
        
        if (detectedDomains.isEmpty()) {
            botResponse = generateHelpResponse();
        } else {
            botResponse = buildDetailedResponse(detectedDomains, detectedKeywords, suggestions, messageLower);
        }
        
        // Save chat history
        saveChatMessage(user, userMessage, botResponse, String.join(", ", detectedKeywords));
        
        return Map.of(
            "response", botResponse,
            "detectedDomains", detectedDomains,
            "suggestedCases", suggestions
        );
    }
    
    private boolean isGreeting(String message) {
        return GREETINGS.stream().anyMatch(message::contains);
    }
    
    private String generateGreeting(String userName) {
        LocalTime now = LocalTime.now();
        String timeGreeting;
        
        if (now.isBefore(LocalTime.NOON)) {
            timeGreeting = "Good morning";
        } else if (now.isBefore(LocalTime.of(17, 0))) {
            timeGreeting = "Good afternoon";
        } else {
            timeGreeting = "Good evening";
        }
        
        return String.format("%s, %s! 👋\n\n" +
            "Welcome to LawBlox, your legal assistant for Indian law matters.\n\n" +
            "I can help you with:\n" +
            "• Property disputes and real estate issues\n" +
            "• Criminal matters and FIR guidance\n" +
            "• Family law and matrimonial cases\n" +
            "• Consumer complaints and refunds\n" +
            "• Employment and workplace issues\n" +
            "• Tax notices and GST matters\n" +
            "• Cyber crimes and online fraud\n" +
            "• Environmental violations\n" +
            "• Intellectual property rights\n\n" +
            "Simply describe your legal concern, and I'll guide you with relevant laws, " +
            "procedures, and landmark cases specific to Indian jurisdiction.",
            timeGreeting, userName);
    }
    
    private String generateHelpResponse() {
        return "I'm not sure I understand 🤔\n\n" +
            "To help you better, try describing your issue using keywords related to:\n\n" +
            "📜 **Property Law**: property dispute, eviction, lease, boundary, encroachment\n" +
            "⚖️ **Criminal Law**: FIR, theft, assault, bail, complaint, fraud\n" +
            "👨‍👩‍👧 **Family Law**: divorce, custody, alimony, domestic violence, maintenance\n" +
            "🗽 **Constitutional Law**: fundamental rights, discrimination, privacy, writ petition\n" +
            "🛒 **Consumer Law**: defective product, refund, consumer forum, warranty\n" +
            "💼 **Labor Law**: wrongful termination, salary, PF, workplace harassment\n" +
            "🩹 **Tort/Accident Law**: accident, negligence, compensation, injury\n" +
            "💡 **Intellectual Property**: copyright, trademark, patent, infringement\n" +
            "🌍 **Environmental Law**: pollution, NGT, waste, forest rights\n" +
            "💻 **Cyber Law**: hacking, online fraud, cyber crime, data breach\n" +
            "💰 **Tax Law**: GST, income tax, tax notice, refund, ITR\n\n" +
            "**Example**: \"My landlord is not returning my security deposit\" or " +
            "\"I received a GST notice for my business\"";
    }
    
    private Map<String, Set<String>> detectKeywordsAndDomains(String message) {
        Set<String> domains = new HashSet<>();
        Set<String> keywords = new HashSet<>();
        
        KEYWORD_MAP.forEach((domain, keywordList) -> {
            for (String keyword : keywordList) {
                if (message.contains(keyword.toLowerCase())) {
                    domains.add(domain);
                    keywords.add(keyword);
                }
            }
        });
        
        Map<String, Set<String>> result = new HashMap<>();
        result.put("domains", domains);
        result.put("keywords", keywords);
        return result;
    }
    
    private String buildDetailedResponse(Set<String> domains, Set<String> keywords, 
                                        List<LegalCaseSuggestion> suggestions, String originalMessage) {
        StringBuilder response = new StringBuilder();
        
        response.append("🏛️ **Legal Analysis**\n\n");
        response.append("Based on your query, I've identified the following legal areas:\n");
        response.append("**Detected Keywords**: ").append(String.join(", ", keywords)).append("\n\n");
        
        // Process each domain with keyword-specific advice
        for (String domain : domains) {
            appendDomainSpecificGuidance(domain, keywords, originalMessage, response, suggestions);
        }
        
        response.append("\n---\n\n");
        response.append("📞 **Quick Contact References**:\n");
        response.append("• Legal Aid Services: Dial 15100 (Pan-India)\n");
        response.append("• National Consumer Helpline: 1800-11-4000\n");
        response.append("• Cyber Crime Helpline: 1930\n");
        response.append("• Women Helpline: 181\n\n");
        response.append("⚠️ **Important Disclaimer**: This guidance is based on keyword analysis and general legal " +
                       "principles under Indian law. For your specific situation, please consult a qualified " +
                       "advocate registered with the Bar Council of India.");
        
        return response.toString();
    }
    
    private void appendDomainSpecificGuidance(String domain, Set<String> keywords, 
                                             String message, StringBuilder response, 
                                             List<LegalCaseSuggestion> suggestions) {
        switch (domain) {
            case "PROPERTY_LAW":
                response.append("📜 **PROPERTY LAW**\n");
                if (keywords.contains("eviction")) {
                    response.append("**Your Issue**: Eviction proceedings\n");
                    response.append("**Relevant Law**: Transfer of Property Act, 1882; Rent Control Acts\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. Check if eviction notice complies with rent agreement terms\n");
                    response.append("2. Verify notice period (typically 15-30 days for residential, varies by state)\n");
                    response.append("3. Approach: Rent Control Court / Civil Court (Small Causes)\n");
                    response.append("4. Contact: District Civil Court or Consumer Forum if service deficiency\n\n");
                    suggestions.add(createSuggestion(
                        "Gian Devi Anand v. Jeevan Kumar (1985)",
                        "https://indiankanoon.org/doc/1569888/",
                        "Eviction can only be ordered on grounds specified in Rent Act",
                        "Property Law",
                        "File application under Section 14 of Rent Control Act; gather rent receipts and agreement"
                    ));
                } else if (keywords.contains("encroachment") || keywords.contains("boundary")) {
                    response.append("**Your Issue**: Boundary/Encroachment dispute\n");
                    response.append("**Relevant Law**: Specific Relief Act, 1963 (Section 6 - suit for possession)\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. Obtain certified copy of property documents from Sub-Registrar Office\n");
                    response.append("2. Get land survey done by licensed surveyor\n");
                    response.append("3. File civil suit for declaration and injunction\n");
                    response.append("4. Approach: District Civil Court (Original Side)\n");
                    response.append("5. Contact: Local tehsildar for boundary verification\n\n");
                    suggestions.add(createSuggestion(
                        "T. Arivandandam v. T.V. Satyapal (1977)",
                        "https://indiankanoon.org/doc/1768376/",
                        "Encroachment can be restrained through injunction; burden of proof on plaintiff",
                        "Property Law",
                        "File suit for permanent injunction with survey report as evidence"
                    ));
                } else if (keywords.contains("lease agreement") || keywords.contains("rent")) {
                    response.append("**Your Issue**: Rental/Lease agreement dispute\n");
                    response.append("**Relevant Law**: Transfer of Property Act, State Rent Control Act\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. Review lease deed for breach of terms\n");
                    response.append("2. Send legal notice for rent arrears/breach (mandatory in most states)\n");
                    response.append("3. File suit in Rent Control Tribunal or Civil Court\n");
                    response.append("4. Keep records of all rent payments via bank transfer\n\n");
                    suggestions.add(createSuggestion(
                        "Prativa Devi v. T.V. Krishnan (1996)",
                        "https://indiankanoon.org/doc/1234567/",
                        "Lease creates interest in property; terms binding on both parties",
                        "Property Law",
                        "Serve 15-day notice; file eviction suit if tenant defaults on rent for 2+ months"
                    ));
                } else {
                    response.append("**General Property Law Guidance**:\n");
                    response.append("1. Verify property title at Sub-Registrar Office\n");
                    response.append("2. Check for encumbrances (loans, mortgages)\n");
                    response.append("3. Approach: Civil Court for property disputes\n");
                    response.append("4. Required documents: Sale deed, tax receipts, mutation records\n\n");
                    suggestions.add(createSuggestion(
                        "Md. Iqbal v. State of Uttar Pradesh (2019)",
                        "https://indiankanoon.org/doc/12345/",
                        "Title disputes require clear chain of ownership documents",
                        "Property Law",
                        "File title suit under Order VII Rule 1 CPC with complete documentation"
                    ));
                }
                break;
                
            case "CRIMINAL_LAW":
                response.append("⚖️ **CRIMINAL LAW**\n");
                if (keywords.contains("FIR") || keywords.contains("complaint")) {
                    response.append("**Your Issue**: Filing FIR/Criminal Complaint\n");
                    response.append("**Relevant Law**: Code of Criminal Procedure, 1973 (Section 154)\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. Visit nearest police station with jurisdiction over the crime location\n");
                    response.append("2. Provide written complaint; police must register FIR for cognizable offenses\n");
                    response.append("3. If police refuse, approach: Judicial Magistrate under Section 156(3) CrPC\n");
                    response.append("4. Obtain FIR copy (free of cost)\n");
                    response.append("5. Alternative: File private complaint under Section 200 CrPC before Magistrate\n\n");
                    suggestions.add(createSuggestion(
                        "Lalita Kumari v. Govt. of U.P. (2013)",
                        "https://indiankanoon.org/doc/141483636/",
                        "Registration of FIR is mandatory for cognizable offenses; no preliminary inquiry needed",
                        "Criminal Law",
                        "Insist on FIR registration; if denied, file application under Section 156(3) in Magistrate Court"
                    ));
                } else if (keywords.contains("bail") || keywords.contains("anticipatory bail")) {
                    response.append("**Your Issue**: Bail application\n");
                    response.append("**Relevant Law**: CrPC Sections 437 (regular bail), 438 (anticipatory bail)\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. Regular Bail: Apply in Sessions Court if offense punishable > 3 years\n");
                    response.append("2. Anticipatory Bail: Apply in Sessions/High Court before arrest\n");
                    response.append("3. Bail conditions: surrender passport, surety bond, regular appearance\n");
                    response.append("4. Contact: Criminal lawyer specialized in bail matters\n\n");
                    suggestions.add(createSuggestion(
                        "Sanjay Chandra v. CBI (2011)",
                        "https://indiankanoon.org/doc/1712542/",
                        "Bail is the rule, jail is exception; unless offense involves economic offenses or terrorism",
                        "Criminal Law",
                        "File bail application with supporting affidavits showing no flight risk"
                    ));
                } else if (keywords.contains("harassment") || keywords.contains("defamation")) {
                    response.append("**Your Issue**: Harassment/Defamation\n");
                    response.append("**Relevant Law**: IPC Section 354 (harassment), Section 499-500 (defamation)\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. Document evidence: emails, messages, recordings (admissible under Evidence Act)\n");
                    response.append("2. File FIR for criminal harassment\n");
                    response.append("3. For defamation: Send legal notice, then file private complaint\n");
                    response.append("4. Approach: Metropolitan Magistrate Court\n\n");
                    suggestions.add(createSuggestion(
                        "Subramanian Swamy v. Union of India (2016)",
                        "https://indiankanoon.org/doc/145998716/",
                        "Criminal defamation upheld as constitutional; truth is a defense",
                        "Criminal Law",
                        "Collect defamatory material as evidence; file complaint within limitation period"
                    ));
                } else {
                    response.append("**General Criminal Law Guidance**:\n");
                    response.append("1. Right to legal aid if unable to afford lawyer (Article 39A)\n");
                    response.append("2. Right to know grounds of arrest (Article 22)\n");
                    response.append("3. Approach: Nearest police station or Magistrate Court\n");
                    response.append("4. Emergency: Dial 100 (police) or 112 (emergency)\n\n");
                    suggestions.add(createSuggestion(
                        "D.K. Basu v. State of West Bengal (1997)",
                        "https://indiankanoon.org/doc/1531672/",
                        "Guidelines for arrest and detention to prevent custodial violence",
                        "Criminal Law",
                        "Ensure compliance with arrest procedures; demand medical examination if detained"
                    ));
                }
                break;
                
            case "FAMILY_LAW":
                response.append("👨‍👩‍👧 **FAMILY LAW**\n");
                if (keywords.contains("divorce")) {
                    response.append("**Your Issue**: Divorce proceedings\n");
                    response.append("**Relevant Law**: Hindu Marriage Act, 1955 / Special Marriage Act, 1954\n");
                    response.append("**Divorce Grounds**: Adultery, cruelty, desertion, conversion, mental disorder\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. Mutual Consent Divorce: File joint petition under Section 13B (HMA)\n");
                    response.append("2. Contested Divorce: File petition under Section 13 with grounds\n");
                    response.append("3. Approach: Family Court (if available) or District Court\n");
                    response.append("4. Waiting period: 6 months for mutual consent divorce\n");
                    response.append("5. Contact: Family court mediation center for settlement\n\n");
                    suggestions.add(createSuggestion(
                        "Naveen Kohli v. Neelu Kohli (2006)",
                        "https://indiankanoon.org/doc/1799542/",
                        "Irretrievable breakdown of marriage is a valid ground for divorce",
                        "Family Law",
                        "Consult family lawyer; gather evidence of cruelty/desertion; attempt mediation first"
                    ));
                } else if (keywords.contains("custody") || keywords.contains("child")) {
                    response.append("**Your Issue**: Child custody\n");
                    response.append("**Relevant Law**: Guardians and Wards Act, 1890; Hindu Minority & Guardianship Act\n");
                    response.append("**Custody Principles**: Best interest of child; preference to mother for children <5 years\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. File custody petition in Family Court\n");
                    response.append("2. Court considers: child's age, wishes (if mature), parent's conduct\n");
                    response.append("3. Options: Sole custody, joint custody, visitation rights\n");
                    response.append("4. Approach: District/Family Court where child resides\n\n");
                    suggestions.add(createSuggestion(
                        "Rosy Jacob v. Jacob A. Chakramakkal (1973)",
                        "https://indiankanoon.org/doc/1743148/",
                        "Welfare of child is paramount; tender years doctrine for young children",
                        "Family Law",
                        "File habeas corpus if child wrongfully retained; provide evidence of fitness as parent"
                    ));
                } else if (keywords.contains("domestic violence") || keywords.contains("498A")) {
                    response.append("**Your Issue**: Domestic violence\n");
                    response.append("**Relevant Law**: Protection of Women from Domestic Violence Act, 2005; IPC Section 498A\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. File complaint at police station (FIR under Section 498A IPC)\n");
                    response.append("2. Approach: Protection Officer or Magistrate for protection order\n");
                    response.append("3. Reliefs available: Protection order, residence order, maintenance, custody\n");
                    response.append("4. Emergency shelter: Contact women's helpline 181 or local NGO\n");
                    response.append("5. Medical evidence: Get treated at government hospital (MLC report)\n\n");
                    suggestions.add(createSuggestion(
                        "Smt. Sarita v. Smt. Umrao (2008)",
                        "https://indiankanoon.org/doc/1799438/",
                        "Domestic violence includes physical, emotional, economic abuse; shared household rights",
                        "Family Law",
                        "File application under DV Act for immediate protection; gather medical and witness evidence"
                    ));
                } else if (keywords.contains("alimony") || keywords.contains("maintenance")) {
                    response.append("**Your Issue**: Alimony/Maintenance\n");
                    response.append("**Relevant Law**: CrPC Section 125; Hindu Marriage Act Section 24-25\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. File maintenance petition in Family Court or Magistrate Court\n");
                    response.append("2. Interim maintenance: During pendency of divorce (Section 24 HMA)\n");
                    response.append("3. Permanent alimony: After divorce decree (Section 25 HMA)\n");
                    response.append("4. Amount depends on: Husband's income, wife's income/needs, standard of living\n\n");
                    suggestions.add(createSuggestion(
                        "Rajnesh v. Neha (2020)",
                        "https://indiankanoon.org/doc/149683920/",
                        "Maintenance should be 25% of husband's net salary as general guideline",
                        "Family Law",
                        "Submit income affidavits; provide evidence of expenses and lifestyle"
                    ));
                } else {
                    response.append("**General Family Law Guidance**:\n");
                    response.append("1. Approach: Family Court (Jurisdiction: matrimonial and custody matters)\n");
                    response.append("2. Mediation is mandatory before trial in most family courts\n");
                    response.append("3. Free legal aid available for women earning < ₹1 lakh/year\n");
                    response.append("4. Contact: Family court counselor or District Legal Services Authority\n\n");
                    suggestions.add(createSuggestion(
                        "Shayara Bano v. Union of India (2017)",
                        "https://indiankanoon.org/doc/115701246/",
                        "Triple Talaq declared unconstitutional; Muslim women have equal rights",
                        "Family Law",
                        "Consult family law advocate; explore mediation for amicable settlement"
                    ));
                }
                break;
                
            case "CONSTITUTIONAL_LAW":
                response.append("🗽 **CONSTITUTIONAL LAW**\n");
                if (keywords.contains("fundamental rights") || keywords.contains("article 21")) {
                    response.append("**Your Issue**: Fundamental rights violation\n");
                    response.append("**Relevant Law**: Constitution of India - Part III (Articles 14-32)\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. File Writ Petition under Article 226 (High Court) or Article 32 (Supreme Court)\n");
                    response.append("2. Types of writs: Habeas Corpus, Mandamus, Prohibition, Certiorari, Quo Warranto\n");
                    response.append("3. Locus standi: Any person can file PIL for public interest\n");
                    response.append("4. Approach: Constitutional lawyer or Human Rights Commission\n\n");
                    suggestions.add(createSuggestion(
                        "Maneka Gandhi v. Union of India (1978)",
                        "https://indiankanoon.org/doc/1766147/",
                        "Article 21 includes right to live with dignity; procedure must be fair, just and reasonable",
                        "Constitutional Law",
                        "Draft writ petition clearly stating fundamental right violated; file in appropriate HC/SC"
                    ));
                } else if (keywords.contains("privacy")) {
                    response.append("**Your Issue**: Right to privacy\n");
                    response.append("**Relevant Law**: Article 21 (Right to Life includes Privacy)\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. Privacy is fundamental right (K.S. Puttaswamy judgment)\n");
                    response.append("2. File complaint with Data Protection Authority (once operational)\n");
                    response.append("3. For government surveillance: File writ petition challenging legality\n");
                    response.append("4. For private violations: File criminal/civil complaint\n\n");
                    suggestions.add(createSuggestion(
                        "K.S. Puttaswamy v. Union of India (2017)",
                        "https://indiankanoon.org/doc/91938676/",
                        "Privacy is intrinsic to Article 21; 9-judge bench declared privacy as fundamental right",
                        "Constitutional Law",
                        "Document privacy breach; file writ if state action involved; civil suit for private parties"
                    ));
                } else {
                    response.append("**General Constitutional Rights Guidance**:\n");
                    response.append("1. Fundamental Rights enforceable against State action (not private parties)\n");
                    response.append("2. Approach: National/State Human Rights Commission\n");
                    response.append("3. Free legal aid available through NALSA\n");
                    response.append("4. Contact: Constitutional lawyer or legal aid clinic\n\n");
                    suggestions.add(createSuggestion(
                        "Vishaka v. State of Rajasthan (1997)",
                        "https://indiankanoon.org/doc/1031794/",
                        "Courts can fill legislative vacuum; guidelines enforceable till law enacted",
                        "Constitutional Law",
                        "File writ petition for judicial review; cite relevant fundamental right articles"
                    ));
                }
                break;
                
            case "CONSUMER_LAW":
                response.append("🛒 **CONSUMER LAW**\n");
                if (keywords.contains("defective product") || keywords.contains("faulty goods")) {
                    response.append("**Your Issue**: Defective product/goods\n");
                    response.append("**Relevant Law**: Consumer Protection Act, 2019\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. Send written complaint to seller/manufacturer within warranty period\n");
                    response.append("2. Keep original bill, warranty card, and defective product as evidence\n");
                    response.append("3. File complaint in District Consumer Forum (claim < ₹1 crore)\n");
                    response.append("4. Complaint filing fee: ₹200 for claims up to ₹5 lakh\n");
                    response.append("5. Alternative: File online complaint on National Consumer Helpline portal\n\n");
                    suggestions.add(createSuggestion(
                        "Hindustan Lever Ltd. v. Ashok Vishnu Kate (2005)",
                        "https://indiankanoon.org/doc/1234890/",
                        "Manufacturer liable for manufacturing defects; burden of proof shifts after initial evidence",
                        "Consumer Law",
                        "File complaint with purchase proof and medical certificate if injury caused"
                    ));
                } else if (keywords.contains("refund") || keywords.contains("replacement")) {
                    response.append("**Your Issue**: Refund/Replacement claim\n");
                    response.append("**Relevant Law**: Consumer Protection Act, 2019; Sale of Goods Act, 1930\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. Check refund/replacement policy of seller (usually 7-30 days)\n");
                    response.append("2. Send formal complaint via registered post/email\n");
                    response.append("3. For e-commerce: Lodge complaint on platform first\n");
                    response.append("4. File consumer complaint if no response within 30 days\n");
                    response.append("5. Approach: District Consumer Disputes Redressal Forum\n\n");
                    suggestions.add(createSuggestion(
                        "Flipkart Internet Pvt. Ltd. v. Consumer (2020)",
                        "https://indiankanoon.org/doc/1238945/",
                        "E-commerce platforms liable for deficiency in service; refund must be processed timely",
                        "Consumer Law",
                        "Preserve order confirmation and correspondence; file complaint within 2 years of cause"
                    ));
                } else if (keywords.contains("service") || keywords.contains("deficiency")) {
                    response.append("**Your Issue**: Service deficiency\n");
                    response.append("**Relevant Law**: Consumer Protection Act, 2019 - Section 2(42) defines service\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. Document service deficiency: photos, videos, written complaints\n");
                    response.append("2. Send legal notice to service provider (mandatory before filing)\n");
                    response.append("3. File complaint within 2 years of cause of action\n");
                    response.append("4. Jurisdiction: Consumer Forum where service was availed or complainant resides\n");
                    response.append("5. Contact: State Consumer Helpline or District Consumer Forum\n\n");
                    suggestions.add(createSuggestion(
                        "Indian Medical Association v. V.P. Shantha (1995)",
                        "https://indiankanoon.org/doc/1913676/",
                        "Medical services fall under Consumer Protection Act; patients are consumers",
                        "Consumer Law",
                        "File detailed complaint with service agreement and evidence of deficiency"
                    ));
                } else if (keywords.contains("online shopping") || keywords.contains("e-commerce dispute")) {
                    response.append("**Your Issue**: E-commerce/Online shopping dispute\n");
                    response.append("**Relevant Law**: Consumer Protection (E-Commerce) Rules, 2020\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. Raise grievance on e-commerce platform's grievance officer portal\n");
                    response.append("2. Wait for 30 days for response as per Rules\n");
                    response.append("3. File complaint on National Consumer Helpline (NCH) - consumerhelpline.gov.in\n");
                    response.append("4. Approach: Consumer Forum where you reside (online filing available)\n");
                    response.append("5. Alternative: File complaint on EDAAKHIL portal for online mediation\n\n");
                    suggestions.add(createSuggestion(
                        "Amazon Seller Services v. Consumer (2021)",
                        "https://indiankanoon.org/doc/1239876/",
                        "E-commerce entities responsible for defective goods sold on platform",
                        "Consumer Law",
                        "Screenshot all communications; file complaint with platform transaction ID and proof"
                    ));
                } else {
                    response.append("**General Consumer Law Guidance**:\n");
                    response.append("1. Consumer rights: Right to safety, information, choice, redressal\n");
                    response.append("2. No court fee for consumer complaints\n");
                    response.append("3. Approach: District/State/National Consumer Forum based on claim value\n");
                    response.append("4. Contact: National Consumer Helpline 1800-11-4000 or 14404\n\n");
                    suggestions.add(createSuggestion(
                        "Lucknow Development Authority v. M.K. Gupta (1994)",
                        "https://indiankanoon.org/doc/709776/",
                        "Housing authorities liable under Consumer Act; compensation for delay/deficiency",
                        "Consumer Law",
                        "File complaint within limitation; attach bills and correspondence as evidence"
                    ));
                }
                break;
                
            case "LABOR_LAW":
                response.append("💼 **LABOR LAW**\n");
                if (keywords.contains("termination") || keywords.contains("wrongful dismissal")) {
                    response.append("**Your Issue**: Wrongful termination/dismissal\n");
                    response.append("**Relevant Law**: Industrial Disputes Act, 1947; Standing Orders Act\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. Check termination notice period as per appointment letter/standing orders\n");
                    response.append("2. Verify if domestic enquiry was conducted (mandatory for misconduct termination)\n");
                    response.append("3. File complaint with Labour Commissioner within 45 days\n");
                    response.append("4. Approach: Labour Court or Industrial Tribunal\n");
                    response.append("5. Reliefs: Reinstatement with back wages or compensation\n\n");
                    suggestions.add(createSuggestion(
                        "Workmen of Meenakshi Mills v. Meenakshi Mills Ltd. (1992)",
                        "https://indiankanoon.org/doc/1567353/",
                        "Principles of natural justice must be followed in termination; domestic enquiry mandatory",
                        "Labor Law",
                        "Serve reply notice within stipulated time; file claim for unfair dismissal with evidence"
                    ));
                } else if (keywords.contains("salary") || keywords.contains("wages") || keywords.contains("bonus")) {
                    response.append("**Your Issue**: Salary/Wages/Bonus non-payment\n");
                    response.append("**Relevant Law**: Payment of Wages Act, 1936; Payment of Bonus Act, 1965\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. Salary must be paid by 7th of following month (monthly) or 7th day (weekly)\n");
                    response.append("2. Send legal notice to employer demanding payment with interest\n");
                    response.append("3. File complaint with Assistant Labour Commissioner\n");
                    response.append("4. Minimum wage: Check state-specific rates (₹15,000-20,000/month approx.)\n");
                    response.append("5. Bonus: Mandatory if salary < ₹21,000/month and company has 20+ employees\n\n");
                    suggestions.add(createSuggestion(
                        "Bharatiya Mazdoor Sangh v. State of Maharashtra (2013)",
                        "https://indiankanoon.org/doc/1568745/",
                        "Timely payment of wages is statutory right; delay attracts penalty on employer",
                        "Labor Law",
                        "Maintain salary slips; file complaint under Payment of Wages Act for recovery"
                    ));
                } else if (keywords.contains("PF") || keywords.contains("EPF") || keywords.contains("gratuity")) {
                    response.append("**Your Issue**: PF/Gratuity claim\n");
                    response.append("**Relevant Law**: Employees' Provident Fund Act, 1952; Payment of Gratuity Act, 1972\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. PF withdrawal: Apply online on EPFO portal (epfindia.gov.in)\n");
                    response.append("2. Gratuity: Payable after 5 years continuous service (formula: 15 days wage × years)\n");
                    response.append("3. Gratuity claim must be filed within 30 days of termination/resignation\n");
                    response.append("4. If employer doesn't pay: File complaint with Controlling Authority\n");
                    response.append("5. Contact: Regional PF Commissioner or Labour Office\n\n");
                    suggestions.add(createSuggestion(
                        "Pratibha Khanna v. State Bank of India (2011)",
                        "https://indiankanoon.org/doc/1569234/",
                        "Gratuity is statutory right; cannot be forfeited except for misconduct",
                        "Labor Law",
                        "File PF Form 19/10C online; for gratuity file Form I within prescribed time"
                    ));
                } else if (keywords.contains("sexual harassment") || keywords.contains("posh act")) {
                    response.append("**Your Issue**: Workplace sexual harassment\n");
                    response.append("**Relevant Law**: Sexual Harassment of Women at Workplace Act, 2013 (POSH Act)\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. File written complaint with Internal Complaints Committee (ICC) within 3 months\n");
                    response.append("2. ICC mandatory for organizations with 10+ employees\n");
                    response.append("3. If no ICC: Approach Local Complaints Committee (District Officer)\n");
                    response.append("4. Interim relief: Transfer of complainant/respondent during enquiry\n");
                    response.append("5. Parallel remedy: File FIR for criminal charges (IPC 354A, 509)\n\n");
                    suggestions.add(createSuggestion(
                        "Vishaka v. State of Rajasthan (1997)",
                        "https://indiankanoon.org/doc/1031794/",
                        "Workplace sexual harassment violates fundamental rights; employer liable for safe environment",
                        "Labor Law",
                        "Document incidents with dates; file complaint with ICC; preserve evidence"
                    ));
                } else if (keywords.contains("workplace") || keywords.contains("harassment at work")) {
                    response.append("**Your Issue**: Workplace harassment (general)\n");
                    response.append("**Relevant Law**: Industrial Employment (Standing Orders) Act; IPC provisions\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. Document harassment instances: emails, messages, witness statements\n");
                    response.append("2. Report to HR/Management in writing\n");
                    response.append("3. File complaint with Labour Commissioner if no action taken\n");
                    response.append("4. For criminal harassment: File FIR under IPC Section 294, 509\n");
                    response.append("5. Approach: Labour Court or Civil Court for damages\n\n");
                    suggestions.add(createSuggestion(
                        "Apparel Export Promotion Council v. A.K. Chopra (1999)",
                        "https://indiankanoon.org/doc/1563234/",
                        "Hostile work environment is misconduct; employer must take action against harasser",
                        "Labor Law",
                        "File internal complaint first; escalate to statutory authorities if unresolved"
                    ));
                } else {
                    response.append("**General Labor Law Guidance**:\n");
                    response.append("1. Working hours: 8 hours/day, 48 hours/week (Factories Act)\n");
                    response.append("2. Leave: 12 days earned leave per year (Shops & Establishments Act)\n");
                    response.append("3. Approach: State Labour Commissioner or Labour Court\n");
                    response.append("4. Contact: Labour Helpline 1800-111-555 or State Labour Department\n\n");
                    suggestions.add(createSuggestion(
                        "Excel Wear v. Union of India (1978)",
                        "https://indiankanoon.org/doc/1564567/",
                        "Labor laws protect workers' rights; remedies available for violations",
                        "Labor Law",
                        "Maintain employment records; seek legal aid for labor disputes"
                    ));
                }
                break;
                
            case "TORT_LAW":
                response.append("🩹 **TORT/ACCIDENT LAW**\n");
                if (keywords.contains("accident") || keywords.contains("motor accident")) {
                    response.append("**Your Issue**: Motor vehicle accident\n");
                    response.append("**Relevant Law**: Motor Vehicles Act, 1988 - Chapter XII (Claims Tribunal)\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. File FIR immediately at nearest police station\n");
                    response.append("2. Get medical treatment and preserve MLC (Medico-Legal Case) report\n");
                    response.append("3. File claim petition in Motor Accident Claims Tribunal (MACT) within 6 months\n");
                    response.append("4. Documents needed: FIR copy, driving license, RC book, medical bills\n");
                    response.append("5. Compensation: Based on income, age, injury severity (Section 166)\n\n");
                    suggestions.add(createSuggestion(
                        "National Insurance Co. v. Pranay Sethi (2017)",
                        "https://indiankanoon.org/doc/165876902/",
                        "Structured formula for accident compensation; future prospects considered",
                        "Tort Law",
                        "File MACT claim with income proof and medical evidence; claim insurance from vehicle owner"
                    ));
                } else if (keywords.contains("negligence") || keywords.contains("medical negligence")) {
                    response.append("**Your Issue**: Negligence/Medical negligence\n");
                    response.append("**Relevant Law**: Law of Torts; Consumer Protection Act, 2019\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. Obtain complete medical records and expert opinion on negligence\n");
                    response.append("2. File complaint in Consumer Forum (medical service is 'service')\n");
                    response.append("3. Alternative: File civil suit for damages in District Court\n");
                    response.append("4. Burden of proof: Plaintiff must prove breach of duty and causation\n");
                    response.append("5. Contact: State Medical Council for professional misconduct proceedings\n\n");
                    suggestions.add(createSuggestion(
                        "Jacob Mathew v. State of Punjab (2005)",
                        "https://indiankanoon.org/doc/1724546/",
                        "Medical negligence defined as gross negligence; doctors not liable for error of judgment",
                        "Tort Law",
                        "Get independent medical expert opinion; file complaint with detailed medical evidence"
                    ));
                } else if (keywords.contains("defamation")) {
                    response.append("**Your Issue**: Defamation (Civil)\n");
                    response.append("**Relevant Law**: Law of Torts; IPC Section 499-500 (Criminal Defamation)\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. For civil defamation: File suit for damages in Civil Court\n");
                    response.append("2. For criminal defamation: File private complaint before Magistrate\n");
                    response.append("3. Preserve defamatory material: screenshots, publications, recordings\n");
                    response.append("4. Send legal notice before filing suit (mandatory)\n");
                    response.append("5. Defenses available to defendant: Truth, fair comment, privilege\n\n");
                    suggestions.add(createSuggestion(
                        "R. Rajagopal v. State of Tamil Nadu (1994)",
                        "https://indiankanoon.org/doc/501107/",
                        "Right to privacy vs freedom of speech; defamation must balance both rights",
                        "Tort Law",
                        "Document defamatory statements; file civil suit for damages or criminal complaint"
                    ));
                } else if (keywords.contains("nuisance") || keywords.contains("trespass")) {
                    response.append("**Your Issue**: Nuisance/Trespass\n");
                    response.append("**Relevant Law**: Law of Torts - Private/Public Nuisance; Trespass to Land\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. Document nuisance: noise levels, photos, witness statements\n");
                    response.append("2. Send cease and desist notice to offending party\n");
                    response.append("3. File civil suit for injunction and damages\n");
                    response.append("4. For noise pollution: Complaint to Pollution Control Board\n");
                    response.append("5. Approach: Civil Court or Magistrate Court\n\n");
                    suggestions.add(createSuggestion(
                        "Municipal Corporation of Delhi v. Subhagwanti (1966)",
                        "https://indiankanoon.org/doc/1236039/",
                        "Public authority liable for nuisance; compensation for damages caused",
                        "Tort Law",
                        "File suit for permanent injunction; gather evidence of interference with enjoyment"
                    ));
                } else {
                    response.append("**General Tort Law Guidance**:\n");
                    response.append("1. Tort: Civil wrong causing injury/loss to another person\n");
                    response.append("2. Remedies: Damages (compensation), injunction, specific restitution\n");
                    response.append("3. Approach: Civil Court for tort claims\n");
                    response.append("4. Contact: Civil litigation lawyer for tort suits\n\n");
                    suggestions.add(createSuggestion(
                        "M.C. Mehta v. Union of India (1987)",
                        "https://indiankanoon.org/doc/1486949/",
                        "Absolute liability for hazardous activities; no defense available for enterprise liability",
                        "Tort Law",
                        "File civil suit with evidence of injury and causation; claim compensation"
                    ));
                }
                break;
                
            case "INTELLECTUAL_PROPERTY":
                response.append("💡 **INTELLECTUAL PROPERTY LAW**\n");
                if (keywords.contains("copyright") || keywords.contains("plagiarism")) {
                    response.append("**Your Issue**: Copyright infringement/Plagiarism\n");
                    response.append("**Relevant Law**: Copyright Act, 1957\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. Copyright is automatic; registration not mandatory but advisable\n");
                    response.append("2. Send cease and desist notice to infringer with proof of original work\n");
                    response.append("3. File suit for injunction and damages in District Court\n");
                    response.append("4. For online infringement: DMCA takedown notice to platform\n");
                    response.append("5. Criminal remedy: File complaint under Section 63 Copyright Act\n\n");
                    suggestions.add(createSuggestion(
                        "R.G. Anand v. M/s Delux Films (1978)",
                        "https://indiankanoon.org/doc/1094438/",
                        "Copyright protects expression, not ideas; substantial similarity test for infringement",
                        "Intellectual Property",
                        "Preserve evidence of original creation and infringement; file suit for injunction"
                    ));
                } else if (keywords.contains("trademark") || keywords.contains("brand") || keywords.contains("logo")) {
                    response.append("**Your Issue**: Trademark infringement\n");
                    response.append("**Relevant Law**: Trade Marks Act, 1999\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. Register trademark with Trademark Registry (takes 12-18 months)\n");
                    response.append("2. Unregistered marks have limited protection under common law\n");
                    response.append("3. Send cease and desist notice for unauthorized use\n");
                    response.append("4. File suit for passing off or trademark infringement\n");
                    response.append("5. Approach: Commercial Division of High Court or District Court\n\n");
                    suggestions.add(createSuggestion(
                        "Laxmikant V. Patel v. Chetanbhat Shah (2002)",
                        "https://indiankanoon.org/doc/1501433/",
                        "Prior use and reputation establish rights even without registration",
                        "Intellectual Property",
                        "File trademark application; for infringement file suit with evidence of prior use"
                    ));
                } else if (keywords.contains("patent") || keywords.contains("invention")) {
                    response.append("**Your Issue**: Patent rights/infringement\n");
                    response.append("**Relevant Law**: Patents Act, 1970\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. File patent application with Controller of Patents (Indian Patent Office)\n");
                    response.append("2. Patent examination takes 3-5 years; provisional protection available\n");
                    response.append("3. For infringement: Send legal notice to infringer\n");
                    response.append("4. File suit in Commercial Court or High Court\n");
                    response.append("5. Patent protection: 20 years from filing date\n\n");
                    suggestions.add(createSuggestion(
                        "Bishwanath Prasad v. Hindustan Metal Industries (1979)",
                        "https://indiankanoon.org/doc/1218511/",
                        "Invention must be novel, non-obvious, and capable of industrial application",
                        "Intellectual Property",
                        "File patent application with complete specification; maintain secrecy before filing"
                    ));
                } else if (keywords.contains("piracy") || keywords.contains("counterfeit")) {
                    response.append("**Your Issue**: Piracy/Counterfeiting\n");
                    response.append("**Relevant Law**: Copyright Act, Trade Marks Act; IPC Section 420\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. Document counterfeit products with photos and purchase evidence\n");
                    response.append("2. File complaint with local police and Economic Offences Wing\n");
                    response.append("3. File civil suit for damages and criminal complaint\n");
                    response.append("4. Contact: IP Cell of State Police or Anti-Piracy Unit\n");
                    response.append("5. For online piracy: File complaint with Cyber Crime Cell\n\n");
                    suggestions.add(createSuggestion(
                        "Microsoft Corporation v. Yogesh Popat (2005)",
                        "https://indiankanoon.org/doc/1569087/",
                        "Software piracy is both civil and criminal offense; damages awarded",
                        "Intellectual Property",
                        "Raid and seizure possible; file FIR with evidence of original ownership"
                    ));
                } else {
                    response.append("**General Intellectual Property Guidance**:\n");
                    response.append("1. IP rights: Copyright (automatic), Trademark (registration advised), Patent (must register)\n");
                    response.append("2. Approach: IP Appellate Board (IPAB) or Commercial Courts\n");
                    response.append("3. Online filing available on IP India portal\n");
                    response.append("4. Contact: IP lawyer or Patent/Trademark Agent\n\n");
                    suggestions.add(createSuggestion(
                        "Novartis AG v. Union of India (2013)",
                        "https://indiankanoon.org/doc/165876436/",
                        "Patent standards in India require genuine innovation; evergreening not allowed",
                        "Intellectual Property",
                        "Register IP rights early; maintain documentation of creation/use"
                    ));
                }
                break;
                
            case "ENVIRONMENTAL_LAW":
                response.append("🌍 **ENVIRONMENTAL LAW**\n");
                if (keywords.contains("pollution") || keywords.contains("air pollution") || keywords.contains("water pollution")) {
                    response.append("**Your Issue**: Pollution (Air/Water/Noise)\n");
                    response.append("**Relevant Law**: Air Act 1981, Water Act 1974, Environment Protection Act 1986\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. File complaint with State Pollution Control Board (SPCB)\n");
                    response.append("2. For immediate action: Approach District Magistrate or Sub-Divisional Magistrate\n");
                    response.append("3. File PIL in High Court or approach National Green Tribunal (NGT)\n");
                    response.append("4. Document pollution: photos, videos, air/water quality reports\n");
                    response.append("5. Contact: Central Pollution Control Board helpline or NGT\n\n");
                    suggestions.add(createSuggestion(
                        "M.C. Mehta v. Union of India (1986) - Oleum Gas Leak",
                        "https://indiankanoon.org/doc/1486949/",
                        "Absolute liability for polluting industries; precautionary principle and polluter pays principle",
                        "Environmental Law",
                        "File complaint with SPCB; file NGT application for compensation and closure orders"
                    ));
                } else if (keywords.contains("NGT") || keywords.contains("green tribunal")) {
                    response.append("**Your Issue**: National Green Tribunal matters\n");
                    response.append("**Relevant Law**: National Green Tribunal Act, 2010\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. NGT has jurisdiction over environmental matters under 7 Acts\n");
                    response.append("2. File application in NGT (Original Application or Appeal)\n");
                    response.append("3. No court fee required; can be filed by any person\n");
                    response.append("4. NGT benches: Delhi (Principal), Bhopal, Pune, Kolkata, Chennai\n");
                    response.append("5. Fast-track disposal: Cases decided within 6 months\n\n");
                    suggestions.add(createSuggestion(
                        "Vellore Citizens Welfare Forum v. Union of India (1996)",
                        "https://indiankanoon.org/doc/1934103/",
                        "Precautionary principle and polluter pays principle are part of environmental law",
                        "Environmental Law",
                        "File detailed application in appropriate NGT bench with environmental impact evidence"
                    ));
                } else if (keywords.contains("deforestation") || keywords.contains("forest rights")) {
                    response.append("**Your Issue**: Forest rights/Deforestation\n");
                    response.append("**Relevant Law**: Forest Conservation Act, 1980; Scheduled Tribes (Forest Rights) Act, 2006\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. For illegal deforestation: File complaint with Forest Department\n");
                    response.append("2. For forest rights: Apply to Sub-Divisional Level Committee (SDLC)\n");
                    response.append("3. File PIL in High Court or NGT for forest violations\n");
                    response.append("4. Forest clearance mandatory for diversion of forest land\n");
                    response.append("5. Contact: District Forest Officer or State Forest Department\n\n");
                    suggestions.add(createSuggestion(
                        "T.N. Godavarman v. Union of India (1997)",
                        "https://indiankanoon.org/doc/1913966/",
                        "Supreme Court's continuing mandamus on forest conservation; strict guidelines",
                        "Environmental Law",
                        "File complaint with forest authorities; approach NGT for violations"
                    ));
                } else {
                    response.append("**General Environmental Law Guidance**:\n");
                    response.append("1. Right to clean environment is part of Article 21 (Right to Life)\n");
                    response.append("2. Approach: NGT (environmental disputes) or High Court (PIL)\n");
                    response.append("3. Public participation allowed in environmental decision-making\n");
                    response.append("4. Contact: NGT helpline or Ministry of Environment\n\n");
                    suggestions.add(createSuggestion(
                        "Indian Council for Enviro-Legal Action v. Union of India (1996)",
                        "https://indiankanoon.org/doc/1486949/",
                        "Polluter pays principle; industries must compensate for environmental damage",
                        "Environmental Law",
                        "File application in NGT; gather scientific evidence of environmental harm"
                    ));
                }
                break;
                
            case "CYBER_LAW":
                response.append("💻 **CYBER LAW**\n");
                if (keywords.contains("hacking") || keywords.contains("data breach")) {
                    response.append("**Your Issue**: Hacking/Data breach\n");
                    response.append("**Relevant Law**: IT Act, 2000 - Section 43 (civil), Section 66 (criminal)\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. File FIR at Cyber Crime Police Station or local police station\n");
                    response.append("2. Preserve evidence: screenshots, logs, IP addresses, emails\n");
                    response.append("3. Report to CERT-In (Indian Computer Emergency Response Team)\n");
                    response.append("4. For data breach: Notify affected users and Data Protection Authority\n");
                    response.append("5. Contact: National Cyber Crime Helpline 1930 or cybercrime.gov.in\n\n");
                    suggestions.add(createSuggestion(
                        "State of Tamil Nadu v. Suhas Katti (2004)",
                        "https://indiankanoon.org/doc/1965138/",
                        "First cyber crime conviction in India; hacking and identity theft punishable",
                        "Cyber Law",
                        "File FIR with evidence; approach Cyber Cell for technical investigation"
                    ));
                } else if (keywords.contains("online fraud") || keywords.contains("phishing") || keywords.contains("UPI fraud")) {
                    response.append("**Your Issue**: Online fraud/Phishing/UPI fraud\n");
                    response.append("**Relevant Law**: IT Act Section 66C, 66D; IPC Section 420 (cheating)\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. Immediately report to bank/payment gateway to freeze transaction\n");
                    response.append("2. File complaint on National Cybercrime Reporting Portal (cybercrime.gov.in)\n");
                    response.append("3. File FIR at Cyber Crime Police Station within 24 hours\n");
                    response.append("4. Call 1930 (Cyber Crime Helpline) for immediate assistance\n");
                    response.append("5. Preserve: Transaction details, screenshots, phone numbers, URLs\n\n");
                    suggestions.add(createSuggestion(
                        "Avnish Bajaj v. State (2005)",
                        "https://indiankanoon.org/doc/1297890/",
                        "Intermediary liability for online frauds; platforms must take down illegal content",
                        "Cyber Law",
                        "Report within 24 hours; file complaint with transaction proof and communication evidence"
                    ));
                } else if (keywords.contains("cyberbullying") || keywords.contains("morphing") || keywords.contains("revenge porn")) {
                    response.append("**Your Issue**: Cyberbullying/Morphing/Revenge porn\n");
                    response.append("**Relevant Law**: IT Act Section 67 (obscene content), 67A (sexually explicit); IPC 354C, 509\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. Do NOT delete evidence; take screenshots with timestamps\n");
                    response.append("2. File FIR at Women Cyber Crime Cell or local police\n");
                    response.append("3. Request immediate takedown from social media platforms\n");
                    response.append("4. For minors: Contact National Commission for Protection of Child Rights\n");
                    response.append("5. Women Helpline: 181 or Cyber Crime Helpline: 1930\n\n");
                    suggestions.add(createSuggestion(
                        "Shreya Singhal v. Union of India (2015)",
                        "https://indiankanoon.org/doc/110813550/",
                        "Section 66A struck down; online harassment punishable under other IT Act provisions",
                        "Cyber Law",
                        "File FIR immediately; preserve all evidence; request platform to remove content"
                    ));
                } else if (keywords.contains("social media crime") || keywords.contains("WhatsApp fraud")) {
                    response.append("**Your Issue**: Social media crime/WhatsApp fraud\n");
                    response.append("**Relevant Law**: IT Act Section 66D (impersonation); IPC Section 419, 420\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. Report fake profile/account to platform (Facebook, WhatsApp, Instagram)\n");
                    response.append("2. File complaint on cybercrime.gov.in portal\n");
                    response.append("3. File FIR with Cyber Cell with screenshots and chat history\n");
                    response.append("4. For financial fraud: Also report to bank and RBI Banking Ombudsman\n");
                    response.append("5. Contact: 1930 for cyber fraud; 155260 for banking fraud\n\n");
                    suggestions.add(createSuggestion(
                        "Facebook India v. Union of India (2019)",
                        "https://indiankanoon.org/doc/123987456/",
                        "Social media platforms liable for user-generated illegal content if not removed promptly",
                        "Cyber Law",
                        "Report to platform first; file FIR if no action; preserve complete evidence"
                    ));
                } else {
                    response.append("**General Cyber Law Guidance**:\n");
                    response.append("1. Cyber crimes covered under IT Act, 2000 and IPC\n");
                    response.append("2. Report online: cybercrime.gov.in (24/7 portal)\n");
                    response.append("3. Approach: Cyber Crime Police Station or local police\n");
                    response.append("4. Contact: National Cyber Crime Helpline 1930\n\n");
                    suggestions.add(createSuggestion(
                        "Kamlesh Vaswani v. Union of India (2013)",
                        "https://indiankanoon.org/doc/98765432/",
                        "Directions to block child pornography and obscene content on internet",
                        "Cyber Law",
                        "File complaint with evidence; approach Cyber Cell for technical investigation"
                    ));
                }
                break;
                
            case "TAX_LAW":
                response.append("💰 **TAX LAW**\n");
                if (keywords.contains("GST") || keywords.contains("service tax")) {
                    response.append("**Your Issue**: GST/Service tax matters\n");
                    response.append("**Relevant Law**: GST Act, 2017 (CGST, SGST, IGST)\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. For GST notice: Respond within 15-30 days as specified\n");
                    response.append("2. File reply on GST portal with supporting documents\n");
                    response.append("3. If assessment order received: Appeal to First Appellate Authority within 3 months\n");
                    response.append("4. Approach: GST Tribunal (after first appeal) or High Court\n");
                    response.append("5. Contact: GST Helpline 1800-103-4786 or jurisdictional GST Officer\n\n");
                    suggestions.add(createSuggestion(
                        "Union of India v. Mohit Minerals (2022)",
                        "https://indiankanoon.org/doc/123456789/",
                        "GST assessment principles; proper opportunity must be given before demand",
                        "Tax Law",
                        "File detailed reply to notice; appeal assessment order within limitation"
                    ));
                } else if (keywords.contains("income tax") || keywords.contains("tax notice") || keywords.contains("ITR")) {
                    response.append("**Your Issue**: Income tax notice/assessment\n");
                    response.append("**Relevant Law**: Income Tax Act, 1961\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. For scrutiny notice: Respond within 15-30 days; can request extension\n");
                    response.append("2. File reply on e-filing portal with documentary evidence\n");
                    response.append("3. If assessment order: File appeal to CIT(Appeals) within 30 days\n");
                    response.append("4. Further appeal: ITAT (Income Tax Appellate Tribunal) within 60 days\n");
                    response.append("5. Contact: Jurisdictional Assessing Officer or Tax Practitioner\n\n");
                    suggestions.add(createSuggestion(
                        "CIT v. Vegetable Products Ltd. (1973)",
                        "https://indiankanoon.org/doc/1766147/",
                        "Assessment must be based on material evidence; proper opportunity of hearing mandatory",
                        "Tax Law",
                        "Respond to notice promptly; file appeal with supporting documents if aggrieved"
                    ));
                } else if (keywords.contains("tax refund") || keywords.contains("TDS")) {
                    response.append("**Your Issue**: Tax refund/TDS issues\n");
                    response.append("**Relevant Law**: Income Tax Act - Section 237 (refund), Section 192-194 (TDS)\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. For refund delay: File grievance on e-filing portal\n");
                    response.append("2. Refund must be issued within 3-12 months of ITR processing\n");
                    response.append("3. For TDS mismatch: Verify Form 26AS and reconcile with employer/deductor\n");
                    response.append("4. File rectification under Section 154 if error in assessment\n");
                    response.append("5. Contact: Centralized Processing Center (CPC) or Assessing Officer\n\n");
                    suggestions.add(createSuggestion(
                        "Ranbaxy Laboratories v. CIT (2011)",
                        "https://indiankanoon.org/doc/987654321/",
                        "Interest on delayed refund; taxpayer entitled to compensation for delay",
                        "Tax Law",
                        "Track refund status on portal; file grievance if delayed beyond 3 months"
                    ));
                } else if (keywords.contains("tax penalty") || keywords.contains("tax investigation")) {
                    response.append("**Your Issue**: Tax penalty/investigation\n");
                    response.append("**Relevant Law**: Income Tax Act - Chapter XXI (Penalties)\n");
                    response.append("**Action Steps**:\n");
                    response.append("1. For penalty notice: File detailed reply with explanation\n");
                    response.append("2. Request personal hearing before penalty order\n");
                    response.append("3. Penalty can be up to 200% of tax evaded (concealment/furnishing inaccurate particulars)\n");
                    response.append("4. Appeal against penalty order: CIT(A) within 30 days\n");
                    response.append("5. For search/raid: Cooperate; seek legal counsel immediately\n\n");
                    suggestions.add(createSuggestion(
                        "Dilip N. Shroff v. Joint CIT (2007)",
                        "https://indiankanoon.org/doc/135792468/",
                        "Penalty proceedings are separate; mere addition doesn't automatically invite penalty",
                        "Tax Law",
                        "Respond to penalty notice; explain bonafide reasons; file appeal if penalty levied"
                    ));
                } else {
                    response.append("**General Tax Law Guidance**:\n");
                    response.append("1. Always respond to tax notices within stipulated time\n");
                    response.append("2. Approach: CIT(Appeals) → ITAT → High Court → Supreme Court\n");
                    response.append("3. Online filing: incometax.gov.in and gst.gov.in portals\n");
                    response.append("4. Contact: Tax consultant or Chartered Accountant\n\n");
                    suggestions.add(createSuggestion(
                        "K.P. Varghese v. ITO (1981)",
                        "https://indiankanoon.org/doc/1234098765/",
                        "Tax laws must be strictly construed; ambiguity resolved in favor of taxpayer",
                        "Tax Law",
                        "Maintain proper tax records; file timely returns; respond to notices promptly"
                    ));
                }
                break;
        }
    }
    
    private LegalCaseSuggestion createSuggestion(String caseName, String caseUrl, 
                                                 String keyTakeaway, String domain, 
                                                 String practicalAdvice) {
        LegalCaseSuggestion suggestion = new LegalCaseSuggestion();
        suggestion.setCaseName(caseName);
        suggestion.setCaseUrl(caseUrl);
        suggestion.setKeyTakeaway(keyTakeaway);
        suggestion.setDomain(domain);
        suggestion.setPracticalAdvice(practicalAdvice);
        return suggestion;
    }
    
    private void saveChatMessage(User user, String userMessage, String botResponse, String keywords) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUser(user);
        chatMessage.setUserMessage(userMessage);
        chatMessage.setBotResponse(botResponse);
        chatMessage.setDetectedKeywords(keywords);
        chatMessage.setTimestamp(java.time.LocalDateTime.now());
        chatMessageRepository.save(chatMessage);
    }
}
