package com.example.helloworldapp
import android.content.Intent
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import com.example.helloworldapp.ui.theme.HelloWorldAppTheme
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.focusable
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import android.net.Uri
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
var authToken by mutableStateOf("")  // Stores JWT token after login/signup



val FantasyFont = FontFamily(
    Font(R.font.f1, weight = FontWeight.Light, style = FontStyle.Italic),
    Font(R.font.f2, weight = FontWeight.Bold, style = FontStyle.Italic)
)


// --------------------------- SCREEN STATE ---------------------------
enum class Screen {
    SPLASH,
    INSTRUCTIONS,
    LOGIN,
    SIGNUP,
    OUR_SERVICES,
    LAW_CASES,
    MustRead,
    LAW_CHAT


}


// --------------------------- MAIN ACTIVITY ---------------------------
// Import this


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HelloWorldAppTheme {
                // Keep track of the current screen
                var currentScreen by remember { mutableStateOf(Screen.SPLASH) }

                // --- BACK BUTTON LOGIC ---
                // We define what happens when the physical back button is pressed
                BackHandler(enabled = currentScreen != Screen.SPLASH) {
                    when (currentScreen) {
                        Screen.INSTRUCTIONS -> {
                            // Optional: Close app or do nothing from Instructions
                            finish()
                        }

                        Screen.LOGIN -> {
                            // If on Login, go back to Instructions
                            currentScreen = Screen.INSTRUCTIONS
                        }

                        Screen.SIGNUP -> {
                            // If on Signup, go back to Instructions (or Login)
                            currentScreen = Screen.INSTRUCTIONS
                        }
                        Screen.LAW_CASES -> {
                            currentScreen = Screen.OUR_SERVICES   // ✅ ADD THIS
                        }
                        Screen.MustRead -> {currentScreen = Screen.OUR_SERVICES}
                        Screen.LAW_CHAT -> {currentScreen = Screen.OUR_SERVICES}

                        else -> { /* Do nothing for Splash */
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    when (currentScreen) {
                        Screen.SPLASH -> LogoScreen {
                            currentScreen = Screen.INSTRUCTIONS
                        }

                        Screen.INSTRUCTIONS -> InstructionScreen(
                            onLoginClick = { currentScreen = Screen.LOGIN },
                            onSignupClick = { currentScreen = Screen.SIGNUP }
                        )

                        Screen.LOGIN -> LoginScreen(
                            onSignupClick = { currentScreen = Screen.SIGNUP },
                            onLoginSuccess = { currentScreen = Screen.OUR_SERVICES } // new callback
                        )

                        Screen.SIGNUP -> SignupScreen(
                            onLoginClick = { currentScreen = Screen.LOGIN }
                        )

                        Screen.OUR_SERVICES -> OurServicesScreen(
                            onServiceClick = { service ->
                                when(service) {
                                    "Case Docket" -> currentScreen = Screen.LAW_CASES
                                    "Judicial Highlights" -> currentScreen = Screen.MustRead   // ✅ Navigate to MustRead
                                    "Court Chat" -> currentScreen = Screen.LAW_CHAT
// Add other services if needed
                                }
                            },
                            onLogout = { currentScreen = Screen.LOGIN }
                        )


                        Screen.LAW_CASES -> LawCasesScreen(
                            onBack = { currentScreen = Screen.OUR_SERVICES }
                        )
                        Screen.MustRead -> MustReadsScreen(onBack = { currentScreen = Screen.OUR_SERVICES })
                        Screen.LAW_CHAT -> Law_Chat {
                            currentScreen = Screen.OUR_SERVICES
                        }


                    }

                }
            }
        }
    }


    // --------------------------- SPLASH SCREEN ---------------------------
    @Composable
    fun LogoScreen(onFinished: () -> Unit) {

        val scale = remember { Animatable(0.6f) }
        val alpha = remember { Animatable(0f) }

        LaunchedEffect(Unit) {
            launch {
                scale.animateTo(1f, tween(1000))
            }
            launch {
                alpha.animateTo(1f, tween(1000))
            }
            delay(1500)
            onFinished()
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.lawblox_logo),
                contentDescription = "lawblox_logo.png",
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer(
                        scaleX = scale.value,
                        scaleY = scale.value
                    )
                    .alpha(alpha.value)
            )
        }
    }


    // --------------------------- INSTRUCTION SCREEN ---------------------------
    @Composable
    fun InstructionScreen(
        onLoginClick: () -> Unit,
        onSignupClick: () -> Unit
    ) {
        val MascotSpace = 200.dp
        val borderColor = Color(0xFF7A5C2E)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {

            ThoughtBubble(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 45.dp,
                        start = 24.dp,
                        end = 24.dp,
                        bottom = MascotSpace
                    )
                    .heightIn(max = 509.dp)
                    .align(Alignment.TopCenter)
            )

            // 🔘 LOGIN / SIGNUP BUTTONS
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ThoughtButton("Login", onLoginClick)
                ThoughtButton("Sign Up", onSignupClick)
            }

            Image(
                painter = painterResource(id = R.drawable.lawmascot),
                contentDescription = "lawmascot.png",
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 12.dp, y = -18.dp)
            )
        }
    }


    // --------------------------- THOUGHT BUBBLE ---------------------------
    @Composable
    fun ThoughtBubble(modifier: Modifier = Modifier) {

        val borderColor = Color(0xFF7A5C2E)

        Box(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 350.dp)
                .drawBehind {
                    val strokeWidth = 2.dp.toPx()
                    val innerPadding = 8.dp.toPx()
                    val accentSize = 20.dp.toPx()

                    drawRect(borderColor, style = Stroke(strokeWidth))
                    drawRect(
                        borderColor,
                        topLeft = Offset(innerPadding, innerPadding),
                        size = size.copy(
                            width = size.width - innerPadding * 2,
                            height = size.height - innerPadding * 2
                        ),
                        style = Stroke(strokeWidth / 2)
                    )

                    // Top Left
                    drawRect(
                        color = borderColor,
                        size = Size(accentSize, accentSize),
                        topLeft = Offset(-strokeWidth, -strokeWidth)
                    )

                    // Top Right
                    drawRect(
                        color = borderColor,
                        size = Size(accentSize, accentSize),
                        topLeft = Offset(size.width - accentSize + strokeWidth, -strokeWidth)
                    )

                    // Bottom Left
                    drawRect(
                        color = borderColor,
                        size = Size(accentSize, accentSize),
                        topLeft = Offset(-strokeWidth, size.height - accentSize + strokeWidth)
                    )

                    // Bottom Right
                    drawRect(
                        color = borderColor,
                        size = Size(accentSize, accentSize),
                        topLeft = Offset(
                            size.width - accentSize + strokeWidth,
                            size.height - accentSize + strokeWidth
                        )
                    )

                }
                .padding(30.dp)
        ) {
            Column {
                // Fixed title - NOT scrollable
                Text(
                    text = "Welcome to LawBlox: The Architect of Legal Literacy.",
                    color = borderColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable content only
                Text(
                    text = """You are experiencing the Alpha build of LawBlox — India's intelligent legal assistant designed to decode the complexities of our justice system. We exist because every citizen deserves to comprehend a legal notice without panic, challenge an unjust termination without fear, and understand their constitutional rights without hiring expensive counsel.
In a nation where a simple property dispute can drain ₹2 lakh in legal fees before the first hearing, where 4.5 crore cases languish in courts, where the average citizen surrenders their rights simply because they couldn't afford to understand them — LawBlox stands as your first line of defense.
🏛️ Our Constitutional Promise
Access to Justice as a Fundamental Right
LawBlox transforms Article 39A from paper to practice. When a property dispute notice arrives at your doorstep, when your landlord refuses your security deposit, when workplace harassment goes unchecked, when a tax notice threatens your business — you shouldn't need ₹50,000 in consultation fees just to understand your legal standing.
We've meticulously mapped 11 critical legal domains spanning Property Law to Cyber Crime, Criminal Procedure to Consumer Protection, Family Law to Environmental Justice — covering the legal landscapes that affect ordinary Indians daily. Our intelligent system processes your concerns through 200+ keyword patterns derived from Indian statutes, landmark Supreme Court judgments, High Court precedents, and the ground realities of district courts across our nation.
⚖️ How LawBlox Empowers You
Instant Legal Intelligence:
Speak naturally about your problem — "My employer hasn't paid my PF for 6 months," "Someone is cyberbullying my daughter," "My landlord is evicting me without notice," or "I received a GST demand I don't understand" — and receive:

Precise Legal Classification across multiple domains (Labor Law, Cyber Law, Property Law, Tax Law)
Applicable Indian Statutes with exact sections (Payment of Gratuity Act, IT Act Section 67, Transfer of Property Act, GST Act 2017)
Step-by-Step Actionable Guidance with exact forum jurisdiction (Labour Commissioner, Cyber Cell, Rent Control Court, GST Tribunal)
Landmark Case Precedents from Supreme Court and High Courts with Indian Kanoon citations
Emergency Contacts strategically curated (1930 for cyber crime, 181 for women in distress, 1800-11-4000 for consumer complaints, 15100 for legal aid)
Timeline Awareness for critical limitation periods and response deadlines

Real-World Scenario Intelligence:
Whether you're facing a Section 498A domestic violence case, navigating GST notice confusion, filing a MACT accident claim, challenging wrongful termination, seeking anticipatory bail, contesting property encroachment, filing consumer complaints against e-commerce platforms, or reporting environmental violations to NGT — LawBlox provides forensic guidance rooted in Indian legal procedure, not theoretical abstractions.
📜 The LawBlox Difference
Accuracy Through Indian Jurisprudence:
Every response references verified legal frameworks tested in Indian courts. From IPC sections to CrPC procedures, Consumer Protection Act guidelines to Motor Vehicles Act provisions, Hindu Marriage Act to IT Act amendments — we cite authentic case law with direct Indian Kanoon links for your deeper research.
Contextual Intelligence:
We don't just detect keywords — we understand context. "Harassment" triggers different legal pathways depending on whether it's workplace sexual harassment (POSH Act, ICC), criminal harassment (IPC 354), cyber harassment (IT Act 67), or tenant harassment (Rent Control Act). Our AI recognizes these nuances.
Privacy as Sacred:
Your legal struggles remain confidential. We don't profile users, track queries for marketing, or commercialize your vulnerabilities. Your midnight search about domestic violence or your weekend worry about a tax notice — these remain between you and LawBlox.
Empowerment Over Exploitation:
We guide you to the precise forum — District Consumer Court for product defects under ₹1 crore, Family Court for custody battles, National Green Tribunal for pollution, MACT for accident compensation, Labour Court for wrongful dismissal — saving you from procedural mazes, jurisdictional confusion, and legal ambulance chasers who profit from your ignorance.
🚀 Beyond Keywords: Our Vision
LawBlox isn't merely matching words to laws. We're building a movement where:

A domestic worker in Bengaluru understands her rights under the Minimum Wages Act
A small business owner in Jaipur confidently responds to a GST notice
A student in Kolkata knows how to file an FIR when the police refuse
A senior citizen in Mumbai challenges an insurance claim rejection
A tribal community in Chhattisgarh learns about Forest Rights Act protections

Because legal literacy is the bedrock of democracy.
⚠️ Know Your Boundaries, Know Your Strength
LawBlox is your legal navigator, not your advocate. We illuminate paths under Indian law, decode complex procedures, and arm you with knowledge — but we cannot represent you in court, file petitions on your behalf, or provide individualized legal advice tailored to case-specific evidence.
For courtroom representation, consult Bar Council of India-registered advocates. For free legal services, approach your District Legal Services Authority (DLSA) or State Legal Services Authority. For emergency situations involving arrest, violence, or immediate danger — contact police (100) or emergency services (112) first, then seek legal counsel.
What LawBlox guarantees: You will never enter a lawyer's office, a police station, or a courtroom without understanding the fundamental legal framework of your situation. You will know which court has jurisdiction, which sections apply, what precedents exist, and what realistic timelines look like.

"Justice delayed is justice denied. Legal ignorance should never be the reason for either."
LawBlox ensures that when the law knocks on your door, you open it with knowledge, not fear. When you walk into that government office, that consumer forum, that police station — you walk in as an informed citizen who knows their rights.
The law was written for you. LawBlox helps you read it.
      """,
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Left,
                    fontWeight = FontWeight.Light,
                    fontStyle = FontStyle.Italic,
                    fontFamily = FantasyFont,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            }

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.Black, CircleShape)
                    .align(Alignment.BottomEnd)
                    .offset(x = (80).dp, y = 20.dp)
            )
        }
    }

    // --------------------------- BUTTON STYLE ---------------------------
    @Composable
    fun ThoughtButton(text: String, onClick: () -> Unit) {
        val borderColor = Color(0xFF7A5C2E)

        Box(
            modifier = Modifier
                .width(160.dp)
                .height(52.dp)
                .border(2.dp, borderColor)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = borderColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                fontFamily = FantasyFont
            )
        }
    }


    // --------------------------- PLACEHOLDER SCREENS ---------------------------


    @Composable
    fun LoginScreen(
        onSignupClick: () -> Unit = {},       // Navigate to SignUp
        onLoginSuccess: () -> Unit = {}       // Navigate on successful login
    ) {
        val borderColor = Color(0xFF7A5C2E)
        var authToken = ""  // This will store your login token
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var focusedField by remember { mutableStateOf("") } // "email", "password", or ""
        var errorMessage by remember { mutableStateOf("") }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .width(320.dp)
                    .border(2.dp, borderColor, RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.85f))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ---------------- Mascot Image ----------------
                Crossfade(targetState = focusedField) { field ->
                    val mascotRes = when (field) {
                        "email" -> R.drawable.loginmascot1
                        "password" -> R.drawable.loginmascot2
                        else -> R.drawable.loginmascot1
                    }
                    Image(
                        painter = painterResource(id = mascotRes),
                        contentDescription = "login mascot",
                        modifier = Modifier.size(255.dp)
                    )
                }

                Spacer(modifier = Modifier.height(-10.dp))

                // ---------------- Title ----------------
                Text(
                    text = "Login to LawBlox",
                    color = borderColor,
                    fontSize = 35.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FantasyFont
                )

                Spacer(modifier = Modifier.height(-2.dp))

                // ---------------- Email Field ----------------
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Email", color = borderColor) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            focusedField = if (focusState.isFocused) "email" else focusedField
                        },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = borderColor,
                        unfocusedTextColor = borderColor,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        cursorColor = borderColor,
                        focusedIndicatorColor = borderColor,
                        unfocusedIndicatorColor = borderColor,
                        focusedPlaceholderColor = borderColor,
                        unfocusedPlaceholderColor = borderColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // ---------------- Password Field ----------------
                var showPassword by remember { mutableStateOf(false) } // new state

                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Password", color = borderColor) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                focusedField = if (focusState.isFocused) "password" else focusedField
                            },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = borderColor,
                            unfocusedTextColor = borderColor,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            cursorColor = borderColor,
                            focusedIndicatorColor = borderColor,
                            unfocusedIndicatorColor = borderColor,
                            focusedPlaceholderColor = borderColor,
                            unfocusedPlaceholderColor = borderColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
                    )

                    // ---------------- Show/Hide Toggle ----------------
                    Text(
                        text = if (showPassword) "Hide" else "Show",
                        color = borderColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 4.dp)
                            .clickable { showPassword = !showPassword }
                    )
                }

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }


                // ---------------- Login Button ----------------
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(52.dp)
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                        .clickable {
                            when {
                                email.isBlank() -> errorMessage = "Email cannot be blank"
                                password.isBlank() -> errorMessage = "Password cannot be blank"
                                else -> {
                                    errorMessage = ""
                                    val request = LoginRequest(email, password)
                                    RetrofitInstance.api.login(request)
                                        .enqueue(object : retrofit2.Callback<AuthResponse> {
                                            override fun onResponse(
                                                call: retrofit2.Call<AuthResponse>,
                                                response: retrofit2.Response<AuthResponse>
                                            ) {
                                                if (response.isSuccessful) {
                                                    AuthSession.token = response.body()?.token ?: ""
                                                    println("Saved token: ${AuthSession.token}")
                                                    onLoginSuccess()
// Navigate to Dashboard or Chat
                                                } else {
                                                    errorMessage =
                                                        "Login failed: ${response.code()}" // shows 401/403
                                                }
                                            }

                                            override fun onFailure(
                                                call: retrofit2.Call<AuthResponse>,
                                                t: Throwable
                                            ) {
                                                errorMessage = "Network error: ${t.message}"
                                            }
                                        })
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Login",
                        color = borderColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // ---------------- Signup Text ----------------
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Don't have an account? ", color = borderColor)
                    Text(
                        "Sign up",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onSignupClick() }
                    )
                }
            }
        }
    }


    @Composable
    fun SignupScreen(
        onLoginClick: () -> Unit = {}
    ) {
        val borderColor = Color(0xFF7A5C2E)

        var firstName by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }

        var focusedField by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") } // For validation errors

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .width(320.dp)
                    .border(2.dp, borderColor, RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.85f))
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ---------------- Mascot ----------------
                Crossfade(targetState = focusedField) { field ->
                    val mascotRes = when (field) {
                        "name", "email" -> R.drawable.loginmascot1
                        "password", "confirmPassword" -> R.drawable.loginmascot2
                        else -> R.drawable.loginmascot1
                    }
                    Image(
                        painter = painterResource(id = mascotRes),
                        contentDescription = "signup mascot",
                        modifier = Modifier.size(255.dp)
                    )
                }

                Spacer(modifier = Modifier.height(-10.dp))

                // ---------------- Title ----------------
                Text(
                    text = "Create Account",
                    color = borderColor,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FantasyFont
                )

                // ---------------- First Name ----------------
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    placeholder = { Text("First Name", color = borderColor) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (it.isFocused) focusedField = "name" },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = borderColor,
                        unfocusedTextColor = borderColor,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = borderColor,
                        focusedIndicatorColor = borderColor,
                        unfocusedIndicatorColor = borderColor,
                        focusedPlaceholderColor = borderColor,
                        unfocusedPlaceholderColor = borderColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // ---------------- Email ----------------
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Email", color = borderColor) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (it.isFocused) focusedField = "email" },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = borderColor,
                        unfocusedTextColor = borderColor,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = borderColor,
                        focusedIndicatorColor = borderColor,
                        unfocusedIndicatorColor = borderColor,
                        focusedPlaceholderColor = borderColor,
                        unfocusedPlaceholderColor = borderColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // ---------------- Password ----------------
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Password", color = borderColor) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (it.isFocused) focusedField = "password" },
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        Text(
                            text = if (passwordVisible) "🙈" else "👁",
                            modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = borderColor,
                        unfocusedTextColor = borderColor,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = borderColor,
                        focusedIndicatorColor = borderColor,
                        unfocusedIndicatorColor = borderColor,
                        focusedPlaceholderColor = borderColor,
                        unfocusedPlaceholderColor = borderColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // ---------------- Confirm Password ----------------
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = { Text("Confirm Password", color = borderColor) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (it.isFocused) focusedField = "confirmPassword" },
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        Text(
                            text = if (passwordVisible) "○" else "●",
                            modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = borderColor,
                        unfocusedTextColor = borderColor,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = borderColor,
                        focusedIndicatorColor = borderColor,
                        unfocusedIndicatorColor = borderColor,
                        focusedPlaceholderColor = borderColor,
                        unfocusedPlaceholderColor = borderColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // ---------------- Validation Error ----------------
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }

// ---------------- Sign Up Button ----------------
                // ---------------- Sign Up Button ----------------
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(52.dp)
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                        .clickable {
                            errorMessage = ""

                            // --- EMAIL VALIDATION ---
                            val emailRegex =
                                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\$".toRegex()

                            when {
                                firstName.isBlank() -> errorMessage = "Please enter your name"
                                !email.matches(emailRegex) -> errorMessage = "Please enter a valid email"
                                password.length < 8 -> errorMessage = "Password must be at least 8 characters"
                                !password.any { it.isUpperCase() } -> errorMessage = "Password must contain at least 1 uppercase letter"
                                !password.any { it.isLowerCase() } -> errorMessage = "Password must contain at least 1 lowercase letter"
                                !password.any { it.isDigit() } -> errorMessage = "Password must contain at least 1 number"
                                !password.any { "!@#$%^&*".contains(it) } -> errorMessage = "Password must contain at least 1 special character"
                                confirmPassword.isBlank() -> errorMessage = "Please confirm your password"
                                password != confirmPassword -> errorMessage = "Passwords do not match"

                                else -> {
                                    // ✅ All validations passed, create request correctly
                                    val request = SignupRequest(
                                        email = email,
                                        password = password,
                                        firstName = firstName
                                    )


                                    // --- Call Backend ---
                                    RetrofitInstance.api.signup(request).enqueue(object : retrofit2.Callback<AuthResponse> {
                                        override fun onResponse(
                                            call: retrofit2.Call<AuthResponse>,
                                            response: retrofit2.Response<AuthResponse>
                                        ) {
                                            if (response.isSuccessful) {
                                                authToken = response.body()?.token ?: ""
                                                println("Signup success! Token: $authToken")
                                                onLoginClick()  // Navigate to Login screen
                                            } else {
                                                errorMessage = "Signup failed: ${response.code()}"
                                                println("Signup failed: ${response.errorBody()?.string()}")
                                            }
                                        }

                                        override fun onFailure(call: retrofit2.Call<AuthResponse>, t: Throwable) {
                                            errorMessage = "Network error: ${t.message}"
                                            println("Signup network error: ${t.message}")
                                        }
                                    })
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sign Up",
                        color = borderColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }


                // ---------------- Login Text ----------------
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Already have an account? ", color = borderColor)
                    Text(
                        "Login",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onLoginClick() }
                    )
                }
            }
        }
    }

    // --------------------------- OUR SERVICES SCREEN ---------------------------
    @Composable
    fun OurServicesScreen(
        onServiceClick: (serviceName: String) -> Unit,
        onLogout: () -> Unit
    )
    {
        val borderColor = Color(0xFF7A5C2E)
        val blockColor = Color(0xFFD4AF37)

        val blockHeight = 90.dp
        val blockSpacing = 16.dp
        val blockCorner = 24.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ---------------- Header (Bigger & Bolder) ----------------
            Text(
                text = "Our Services",
                fontFamily = FantasyFont,
                fontSize = 42.sp,             // Increased from 32sp
                fontWeight = FontWeight.ExtraBold, // Added ExtraBold
                color = borderColor,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ---------------- Service Blocks ----------------
            val services = listOf("Case Docket", "Court Chat", "Judicial Highlights" )

            services.forEach { service ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(blockHeight)
                        .border(2.dp, borderColor, RoundedCornerShape(blockCorner))
                        .background(blockColor.copy(alpha = 0.9f), RoundedCornerShape(blockCorner))
                        .clickable { onServiceClick(service) },
                    contentAlignment = Alignment.Center
                ) {
                    // ---------------- Block Text (Bigger & Bolder) ----------------
                    Text(
                        text = service,
                        fontFamily = FantasyFont,
                        fontSize = 28.sp,         // Increased from 22sp
                        fontWeight = FontWeight.Bold, // Bold (but less than Header)
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(blockSpacing))
            }

            Spacer(modifier = Modifier.height(16.dp))
            // ---------------- Logout Button ----------------
            Box(
                modifier = Modifier
                    .width(160.dp) // smaller than service blocks
                    .height(48.dp)
                    .border(2.dp, borderColor, RoundedCornerShape(18.dp))
                    .background(Color.Black, RoundedCornerShape(18.dp))
                    .clickable { onLogout() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Logout",
                    fontFamily = FantasyFont,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = borderColor
                )
            }

            Spacer(modifier = Modifier.height(24.dp))


            // ---------------- Large Mascot ----------------
            Image(
                painter = painterResource(id = R.drawable.ourservices),
                contentDescription = "Lawblox Mascot",
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .height(300.dp), // Pushed a bit taller
                contentScale = ContentScale.Fit
            )
        }
    }
}
    // --------------------------- OUR SERVICES SCREEN ---------------------------
// Data model for your library
    data class LawCase(
        val title: String,
        val rule: String,
        val solution: String,
        val detailUrl: String // Add this
    )


@Composable
fun LawCasesScreen(onBack: () -> Unit) {
    val borderColor = Color(0xFF7A5C2E)

    val cases = listOf(
        LawCase(
            "Kesavananda Bharati v. State of Kerala (1973)",
            "Constitutional Law",
            "The Supreme Court established the 'Basic Structure' doctrine, limiting Parliament’s power to amend the Constitution by holding certain fundamental features inviolable, safeguarding democracy and fundamental rights. This case is one of the cornerstones of Indian constitutional law.",
            "https://indiankanoon.org/doc/257876/" // Kesavananda Bharati full judgment
        ),

        LawCase(
            "Maneka Gandhi v. Union of India (1978)",
            "Fundamental Rights",
            "Expanded the interpretation of Article 21 (Right to Life and Personal Liberty) to require that laws affecting personal liberty be fair, just, and reasonable, significantly strengthening due process protections under the Constitution.",
            "https://indiankanoon.org/search/?formInput=Maneka+Gandhi+Passport+Case+1978"
        ),

        LawCase(
            "Mohd. Ahmad Khan v. Shah Bano Begum (1985)",
            "Family & Gender Law",
            "The Supreme Court upheld a Muslim woman’s right to maintenance under Section 125 of the CrPC, affirming that secular law applies equally to all citizens and promoting gender justice.",
            "https://indiankanoon.org/search/?formInput=Shah+Bano+Begum+1985"
        ),

        LawCase(
            "Puttaswamy v. Union of India (2017)",
            "Privacy & Fundamental Rights",
            "Recognised the Right to Privacy as a fundamental right under Articles 14, 19 and 21, and laid the groundwork for later rulings on privacy and personal autonomy.",
            "https://indiankanoon.org/search/?formInput=Puttaswamy+v+Union+of+India+2017"
        ),

        LawCase(
            "Navtej Singh Johar v. Union of India (2018)",
            "LGBTQ+ Rights",
            "The Supreme Court decriminalised consensual same‑sex relations by striking down the colonial Section 377 IPC to the extent it applied to private consensual acts, affirming dignity and equality.",
            "https://indiankanoon.org/doc/168671544/" // Navtej Singh Johar judgment
        ),

        LawCase(
            "Triple Talaq Case (Shayara Bano v. Union of India) (2017)",
            "Personal Law",
            "The Supreme Court declared the practice of instant triple talaq unconstitutional, leading to legislative reform and enhanced protections for Muslim women’s rights.",
            "https://indiankanoon.org/search/?formInput=Shayara+Bano+v+Union+of+India+2017"
        ),

        LawCase(
            "Sabarimala Temple Entry Case (2018)",
            "Religious Equality",
            "The Supreme Court ruled that banning women aged 10–50 from entering the Sabarimala temple violated constitutional equality, advancing gender non‑discrimination in religious practices.",
            "https://indiankanoon.org/search/?formInput=Sabarimala+Temple+Entry+Case+2018"
        ),

        LawCase(
            "2G Spectrum Scam Case (2012)",
            "Corruption & Administrative Law",
            "The Supreme Court cancelled 122 telecom licences allocated through arbitrary procedures in a massive corruption scandal, reinforcing transparency and fairness in governance.",
            "https://indiankanoon.org/search/?formInput=2G+Spectrum+2012"
        ),

        LawCase(
            "Bhopal Gas Tragedy Case",
            "Environmental & Civil Justice",
            "Following the Union Carbide disaster, courts oversaw compensation processes and set important precedents in environmental liability and corporate accountability.",
            "https://indiankanoon.org/search/?formInput=Bhopal+Gas+Tragedy+Case"
        ),

        LawCase(
            "K.M. Nanavati v. State of Maharashtra (1959)",
            "Criminal Law",
            "One of India’s most famous murder trials, this case significantly influenced criminal jury trial procedures and reshaped jury usage in Indian courts.",
            "https://indiankanoon.org/search/?formInput=Nanavati+1959"
        ),

        LawCase(
            "Indira Gandhi v. Raj Narain (1975)",
            "Election & Constitutional Law",
            "This landmark election case scrutinised misuse of constitutional amendments and electoral malpractice, reaffirming judicial review and protecting democratic processes.",
            "https://indiankanoon.org/search/?formInput=Indira+Gandhi+v+Raj+Narain+1975"
        ),

        LawCase(
            "Right to Privacy Case (2017)",
            "Personal Liberties",
            "A major Supreme Court judgment establishing the right to privacy as an inherent part of life and liberty under the Constitution, influencing many subsequent civil liberties cases.",
            "https://indiankanoon.org/search/?formInput=Puttaswamy+Privacy+2017"
        ),

        LawCase(
            "Vishaka v. State of Rajasthan (1997)",
            "Workplace Rights & Gender Justice",
            "The Supreme Court laid down the Vishaka Guidelines to combat sexual harassment at the workplace, filling legislation gaps until specific laws were passed.",
            "https://indiankanoon.org/search/?formInput=Vishaka+Guidelines+1997"
        ),

        LawCase(
            "Jessica Lal Murder Case (2006)",
            "Criminal Justice & Media Accountability",
            "The case saw eventual conviction after significant public pressure following an initial acquittal, underscoring the role of public opinion and media in criminal justice.",
            "https://indiankanoon.org/search/?formInput=Jessica+Lal+Murder+Case+2006"
        ),

        LawCase(
            "Aruna Shanbaug Case (2011)",
            "Medical & Ethical Law",
            "The Supreme Court permitted passive euthanasia under strict guidelines, shaping future discourse on end‑of‑life care and autonomy.",
            "https://indiankanoon.org/search/?formInput=Aruna+Shanbaug+Case+2011"
        )
    )



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {

        // ---------------- FIXED HEADER ----------------
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "←",
                color = borderColor,
                fontSize = 27.sp,
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(end = 16.dp)
            )

            Text(
                text = "Case Docket",
                color = borderColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FantasyFont
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ---------------- SCROLLABLE CONTENT ----------------
        Column(
            modifier = Modifier
                .fillMaxSize()          // takes remaining space
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            cases.forEach { lawCase ->
                CaseCard(lawCase, borderColor)
            }
        }
    }
}


@Composable
fun CaseCard(lawCase: LawCase, borderColor: Color) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .background(borderColor.copy(alpha = 0.1f))
            .padding(16.dp)
    ) {
        Text(
            text = lawCase.title,
            color = borderColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FantasyFont
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Rule: ${lawCase.rule}",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = lawCase.solution,
            color = Color.LightGray,
            fontSize = 14.sp,
            fontStyle = FontStyle.Italic
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Read More",
            color = borderColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.clickable {
                // Open the URL in a browser
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(lawCase.detailUrl))
                context.startActivity(intent)
            }
        )
    }
}
// --------------------------- MUST READS SCREEN ---------------------------
data class MustRead(
    val title: String,
    val impact: String,
    val source: String,
    val whyItMatters: String,
    val detailUrl: String,
    val imageRes: Int
)

@Composable
fun MustReadsScreen(onBack: () -> Unit) {
    val borderColor = Color(0xFF7A5C2E)

    val mustReads = listOf(
        MustRead(
            "Sabarimala Temple Entry Case (Indian Young Lawyers Assn. v. State of Kerala, 2018)",
            "Supreme Court struck down centuries‑old practice of barring women (10–50 yrs) from entering Sabarimala temple as unconstitutional discrimination, reinforcing that women have equal right to worship under the Constitution.",
            "The Hindu Centre",
            "A landmark ruling affirming gender equality and freedom of religion against discriminatory religious customs.",
            "https://indiankanoon.org/doc/148566744/",
            R.drawable.sabarimala
        ),
        MustRead(
            "Sarla Mudgal v. Union of India (1995)",
            "Supreme Court clarified that a Hindu husband cannot simply convert to Islam to evade anti‑bigamy laws; highlighted conflicts between religious personal laws and secular criminal law, and flagged Uniform Civil Code debate.",
            "Wikipedia",
            "Major in legal reform discussions on religion, marriage, and gender equality.",
            "https://indiankanoon.org/search/?formInput=Sarla+Mudgal+1995",
            R.drawable.sarla
        ),
        MustRead(
            "Gaurav Jain v. Union of India (1997)",
            "Challenged discriminatory temple entry practices; reinforced that state cannot allow exclusion at religious places on arbitrary grounds, strengthening religious equality.",
            "Why it matters",
            "Expanded the scope of freedom of religion and reinforced right to worship.",
            "https://indiankanoon.org/search/?formInput=Gaurav+Jain+temple+entry",
            R.drawable.gaurav
        ),
        MustRead(
            "Animal Welfare Board of India v. A. Nagaraja & Ors. (2014)",
            "Supreme Court banned jallikattu and use of bulls as performing animals, holding that animals have rights under Article 21 (right to life) and Article 51A(duty to protect the environment), recognizing animal welfare as part of constitutional law.",
            "Animal Law",
            "First high‑profile case defining animal rights under Indian Constitution (treat animals with dignity).",
            "https://indiankanoon.org/doc/39696860/",
            R.drawable.animal
        ),
        MustRead(
            "State of Andhra Pradesh v. Abdul Khader (1999 HC case)",
            "Andhra Pradesh High Court upheld the ban on animal/bird sacrifice in temples, holding such practices are not essential parts of religion and can be regulated under animal cruelty laws.",
            "Legal Service India",
            "Protected animals from cruel religious sacrifice rites.",
            "https://indiankanoon.org/search/?formInput=Andhra+Pradesh+v+Abdul+Khader+animal+sacrifice",
            R.drawable.state
        ),
        MustRead(
            "Cockfighting Prohibition Cases (AP/Telangana)",
            "High Courts repeatedly banned traditional cockfighting despite cultural claims, holding that religious or traditional practices cannot justify cruelty under the Prevention of Cruelty to Animals Act.",
            "Legal Service India",
            "Important for animal welfare jurisprudence against traditional cruelty.",
            "https://indiankanoon.org/search/?formInput=cockfighting+ban+court",
            R.drawable.cockfighting
        ),
        MustRead(
            "Vishaka & Ors. v. State of Rajasthan (1997)",
            "Supreme Court established Vishaka Guidelines to prevent sexual harassment at workplace — filling a legislative void until a dedicated law was enacted in 2013.",
            "Wikipedia",
            "Historic for women’s workplace rights and institutional protections.",
            "https://indiankanoon.org/doc/1404959/",
            R.drawable.vishaka
        ),
        MustRead(
            "Mary Roy v. State of Kerala (1986)",
            "Supreme Court ensured inheritance rights for Syrian Christian women under secular law, overriding discriminatory religious personal law provisions.",
            "Why it matters",
            "Boosted gender equality over religious custom in family law.",
            "https://indiankanoon.org/search/?formInput=Mary+Roy+1986",
            R.drawable.mary
        ),
        MustRead(
            "Surinder Singh Kanda v. Union of India",
            "Although not directly about women, this case shaped due process in disciplinary proceedings (e.g., for female employees) by mandating procedural fairness for government employees.",
            "Why it matters",
            "Has implications for employee rights irrespective of gender.",
            "https://indiankanoon.org/search/?formInput=Surinder+Singh+Kanda",
            R.drawable.surinder
        ),
        MustRead(
            "Delhi Domestic Working Women’s Forum v. Union of India (1995)",
            "Expanded protection for women domestic workers against harassment and exploitation — pushed for protective labor norms for informal sector women.",
            "Why it matters",
            "Recognized rights of economically vulnerable women outside formal jobs.",
            "https://indiankanoon.org/search/?formInput=Delhi+Domestic+Working+Women%27s+Forum",
            R.drawable.delhi
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {

        // ---------------- FIXED HEADER ----------------
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "←",
                color = borderColor,
                fontSize = 27.sp,
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(end = 16.dp)
            )

            Text(
                text = "Judicial Highlights",
                color = borderColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FantasyFont
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ---------------- SCROLLABLE CARDS ----------------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            mustReads.forEach { item ->
                MustReadCard(item, borderColor)
            }
        }
    }
}

@Composable
fun MustReadCard(item: MustRead, borderColor: Color) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .background(borderColor.copy(alpha = 0.1f))
            .padding(16.dp)
    ) {
        // ---------------- IMAGE ----------------
        Image(
            painter = painterResource(id = item.imageRes),
            contentDescription = item.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ---------------- TITLE ----------------
        Text(
            text = item.title,
            color = borderColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FantasyFont
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ---------------- IMPACT ----------------
        Text(
            text = "Impact: ${item.impact}",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(4.dp))

        // ---------------- SOURCE ----------------
        Text(
            text = "Source: ${item.source}",
            color = Color.LightGray,
            fontSize = 14.sp,
            fontStyle = FontStyle.Italic
        )

        Spacer(modifier = Modifier.height(4.dp))

        // ---------------- WHY IT MATTERS ----------------
        Text(
            text = "Why it matters: ${item.whyItMatters}",
            color = Color.White,
            fontSize = 14.sp,
            fontStyle = FontStyle.Italic
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ---------------- READ MORE ----------------
        Text(
            text = "Read More",
            color = borderColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.detailUrl))
                context.startActivity(intent)
            }
        )
    }
}
data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

@Composable
fun Law_Chat(onBack: () -> Unit) {

    val borderColor = Color(0xFF7A5C2E)
    var inputText by remember { mutableStateOf("") }

    // Fake messages until backend exists
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                "Greetings from Court Chat. Describe your matter in brief, and I will highlight the pertinent legal provisions and remedies available.",
                isUser = false
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {

        // ---------------- HEADER ----------------
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "←",
                color = borderColor,
                fontSize = 27.sp,
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(end = 16.dp)
            )

            Text(
                text = "Court Chat",
                color = borderColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FantasyFont
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ---------------- CHAT AREA (SCROLLABLE) ----------------
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            messages.forEach { msg ->
                ChatBubble(message = msg, borderColor = borderColor)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ---------------- INPUT AREA ----------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text("Type your concern to get legal insight…", color = borderColor)
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = borderColor,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = borderColor,
                    unfocusedIndicatorColor = borderColor,
                    focusedPlaceholderColor = borderColor,
                    unfocusedPlaceholderColor = borderColor
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .border(2.dp, borderColor, RoundedCornerShape(14.dp))
                    .clickable {
                        if (inputText.isNotBlank()) {
                            messages.add(ChatMessage(inputText, true))

                            // ✅ Call backend chat endpoint
                            val request = ChatRequest(inputText)
                            RetrofitInstance.api.sendMessage(
                                request,
                                "Bearer ${AuthSession.token}"
                            )
                                .enqueue(object: retrofit2.Callback<Map<String, Any>> {
                                    override fun onResponse(call: retrofit2.Call<Map<String, Any>>, response: retrofit2.Response<Map<String, Any>>) {
                                        if (response.isSuccessful) {
                                            val botReply = response.body()?.get("response")?.toString()
                                                ?: "No response from server"

                                            messages.add(ChatMessage(botReply, false))
                                        } else {
                                            messages.add(ChatMessage("Chat failed: ${response.code()}", false))
                                        }
                                    }

                                    override fun onFailure(call: retrofit2.Call<Map<String, Any>>, t: Throwable) {
                                        messages.add(ChatMessage("Network error: ${t.message}", false))
                                    }
                                })

                            inputText = ""
                        }
                    }
                ,
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "➤",
                    color = borderColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
@Composable
fun ChatBubble(message: ChatMessage, borderColor: Color) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser)
            Arrangement.End
        else
            Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .border(
                    2.dp,
                    borderColor,
                    RoundedCornerShape(16.dp)
                )
                .background(
                    if (message.isUser)
                        borderColor.copy(alpha = 0.15f)
                    else
                        Color.Black,
                    RoundedCornerShape(16.dp)
                )
                .padding(14.dp)
        ) {
            Text(
                text = message.text,
                color = if (message.isUser) Color.White else Color.White ,
                fontSize = 15.sp,
                fontFamily = FantasyFont,
                fontStyle = FontStyle.Italic
            )
        }
    }
}



