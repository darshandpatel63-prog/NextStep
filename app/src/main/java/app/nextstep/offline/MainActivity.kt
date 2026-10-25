package app.nextstep.offline

import android.app.Activity
import android.os.Bundle
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.*
import org.json.JSONArray

private data class Guide(
    val id: String, val category: String, val title: String, val summary: String,
    val steps: List<String>, val checklist: List<String>, val message: String, val caution: String
)

class MainActivity : Activity() {
    private val bg = Color.rgb(246, 248, 252)
    private val ink = Color.rgb(26, 37, 58)
    private val blue = Color.rgb(39, 91, 210)
    private val prefs by lazy { getSharedPreferences("nextstep_local", MODE_PRIVATE) }
    private lateinit var root: LinearLayout

    private val guides = listOf(
        Guide("payment","MONEY & SHOPPING","Payment debited, order not confirmed",
            "A cautious checklist when a payment appears successful but the order is still pending.",
            listOf("Check the merchant app/site order history before paying again.","Save the transaction reference, date, amount and status shown by your bank or payment app.","Check whether the merchant shows the order as pending or failed.","Contact support through the merchant's official app/site and save the ticket number.","If unresolved, follow the bank/payment provider's official dispute process. Never share an OTP, PIN or full card details."),
            listOf("Checked order history","Saved transaction reference","Contacted official support","Saved ticket number"),
            "Hello, my payment of [amount] on [date/time] appears debited, but my order/service is not confirmed. Transaction reference: [reference]. Please check the status and advise the next step. Ticket: [if any].",
            "Do not pay a second time until you check the order and payment status. Reversal timelines vary by provider and payment method."),
        Guide("delivery","MONEY & SHOPPING","Wrong, damaged or missing delivery",
            "Organize evidence and use the seller or platform's official process.",
            listOf("Keep the order ID, invoice and delivery date.","Photograph the package, label and issue before discarding packaging, where practical.","Open the order in the official shopping app/site and choose its return, replacement or help option.","Describe the issue and upload only evidence requested by the official channel.","Save the support ticket and follow the platform's stated process if unresolved."),
            listOf("Saved order ID/invoice","Captured relevant photos","Submitted official request","Saved ticket number"),
            "Hello, I received order [order ID] on [date], but the item is [wrong/damaged/missing]. Please advise the available resolution under your policy. Ticket/reference: [if available].",
            "Eligibility and deadlines depend on the seller, product and platform policy. Check the terms shown in your order."),
        Guide("form","FORMS & APPLICATIONS","Application or form rejected",
            "Turn a rejection notice into a clear list of checks.",
            listOf("Read the full notice and identify the exact reason, deadline and reference number.","Compare requested fields and documents with the official instructions.","Check for spelling mismatches, missing pages, unreadable scans, expired documents or wrong file format.","Correct only issues supported by official instructions; never alter a genuine document's contents.","If unclear or a deadline is near, contact the official helpdesk and keep a record."),
            listOf("Saved notice/reference","Identified stated reason","Checked official requirements","Recorded deadline","Contacted helpdesk if needed"),
            "Hello, my application/reference number is [number]. The notice says [exact reason]. Could you clarify the correction required and the official resubmission or appeal process? Thank you.",
            "Requirements vary by service. NextStep is not a government portal and cannot guarantee approval."),
        Guide("scam","DIGITAL SAFETY","Suspicious message or payment request",
            "Pause, verify independently and protect sensitive information.",
            listOf("Do not click links, install apps, scan payment QR codes or call numbers from the suspicious message.","Never share OTPs, UPI PINs, passwords, card PINs or screen-sharing access.","Verify the claim through the organisation's official app/site or a contact you find independently.","If you paid or shared credentials, contact your bank/provider immediately through its official channel and secure the account.","In India, use official cybercrime reporting channels when appropriate; preserve messages and transaction details."),
            listOf("Did not click/pay/share OTP","Verified independently","Contacted bank/provider if affected","Saved evidence"),
            "I received a suspicious message/request claiming to be from [organisation/person] at [date/time]. It asks for [brief description]. Please confirm whether this is genuine. (Send only to a contact verified independently.)",
            "Do not use a phone number or link from the suspicious message to verify it. Never enter sensitive information into this app.")
    )

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); home() }

    private fun screen(title: String, subtitle: String? = null) {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(22), dp(20), dp(28))
            setBackgroundColor(bg)
        }
        setContentView(ScrollView(this).apply { addView(root) })
        root.addView(TextView(this).apply {
            text = title; textSize = 29f; setTextColor(ink); typeface = Typeface.DEFAULT_BOLD
        })
        if (subtitle != null) text(subtitle)
    }

    private fun home() {
        screen("NextStep", "Everyday problems. One clear next step.\nOffline guides • No account • Checklist saved on this phone")
        val search = EditText(this).apply { hint = "Search guides"; isSingleLine = true; textSize = 16f }
        root.addView(search, params())
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(list)
        fun populate(q: String) {
            list.removeAllViews()
            guides.filter { (it.title + it.category + it.summary).contains(q, true) }.forEach { g ->
                button("${g.category}\n${g.title}\n${g.summary}") { detail(g) }.also { list.addView(it, params()) }
            }
            if (list.childCount == 0) text("No matching guide yet.")
        }
        search.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { populate(s?.toString() ?: "") }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        populate("")
        button("My saved progress") { progress() }
        text("Important: NextStep is an independent guide, not a bank, government service or legal adviser. Verify current instructions with the official provider.")
    }

    private fun detail(g: Guide) {
        screen(g.title, g.summary)
        text("STEP-BY-STEP")
        g.steps.forEachIndexed { i, s -> text("${i + 1}. $s") }
        text("YOUR CHECKLIST")
        val old = load(g.id)
        val boxes = mutableListOf<CheckBox>()
        g.checklist.forEachIndexed { i, label ->
            val cb = CheckBox(this).apply {
                text = label; textSize = 15f; setTextColor(ink); isChecked = old.optBoolean(i, false)
            }
            boxes.add(cb); root.addView(cb)
            cb.setOnCheckedChangeListener { _, _ -> save(g.id, boxes.map { it.isChecked }) }
        }
        text("MESSAGE TEMPLATE")
        text(g.message)
        button("Copy message template") {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("NextStep message", g.message))
            Toast.makeText(this, "Copied. Fill placeholders before sending.", Toast.LENGTH_LONG).show()
        }
        text("PLEASE NOTE")
        text(g.caution)
        button("Save checklist progress") { save(g.id, boxes.map { it.isChecked }); Toast.makeText(this, "Saved on this phone", Toast.LENGTH_SHORT).show() }
        button("Back to guides") { home() }
    }

    private fun progress() {
        screen("My progress", "Checklist status stored locally on this phone.")
        guides.forEach { g ->
            val a = load(g.id)
            text("${g.title}\n${g.checklist.indices.count { a.optBoolean(it, false) }} of ${g.checklist.size} steps checked")
        }
        button("Back to guides") { home() }
    }

    private fun text(value: String) {
        root.addView(TextView(this).apply {
            text = value; textSize = 15f; setTextColor(ink)
            setPadding(0, dp(10), 0, dp(8))
        })
    }

    private fun button(label: String, action: () -> Unit): Button =
        Button(this).apply {
            text = label; isAllCaps = false; textSize = 15f; setTextColor(Color.WHITE)
            setBackgroundColor(blue); setPadding(dp(12), dp(8), dp(12), dp(8))
            setOnClickListener { action() }
        }

    private fun load(id: String): JSONArray = try { JSONArray(prefs.getString(id, "[]")) } catch (_: Exception) { JSONArray() }
    private fun save(id: String, values: List<Boolean>) {
        val a = JSONArray(); values.forEach { a.put(it) }
        prefs.edit().putString(id, a.toString()).apply()
    }
    private fun params() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(10) }
    private fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()
}
