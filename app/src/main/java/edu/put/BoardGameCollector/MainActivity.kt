package edu.put.BoardGameCollector

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.lang.Exception
import java.net.MalformedURLException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity : AppCompatActivity() {
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
    var username: String? = null
    var tempLastSync: String? = null
    private val REQUEST_CODE = 10000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val filename =  "$filesDir/username.txt"
        val file = File(filename)
        if(file.exists()){
            val data: List<String> = File(filename).bufferedReader().readLines()
            username = data[0]
            findViewById<TextView>(R.id.lastSyncView).text = getString(R.string.last_sync)+" ${data[1]}"
            findViewById<TextView>(R.id.numGamesView).text = getString(R.string.number_games)+" ${data[2]}"
            findViewById<TextView>(R.id.numExpansionsView).text = getString(R.string.number_expansions)+" ${data[3]}"
            findViewById<TextView>(R.id.usernameView).text = getString(R.string.username)+" $username"
        }
        if(username == null){
            File("$filesDir/thumbnails/").mkdir()
            val i = Intent(this, FirstTimeActivity::class.java)
            startActivityForResult(i, REQUEST_CODE)
        }
    }
    fun gamesClick(v: View) {
        val i = Intent(this, GamesActivity::class.java)
        startActivity(i)
    }
    fun expansionsClick(v: View) {
        val i = Intent(this, ExpansionsActivity::class.java)
        startActivity(i)
    }
    fun resetClick(v: View) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.confirm_reset))

            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                val dbHandler = MyDBHandler(this, null, null, 1)
                dbHandler.reset()
                username = null
                File("$filesDir/").deleteRecursively()
                File("$filesDir/thumbnails/").mkdir()
                //finish()
                val i = Intent(this, FirstTimeActivity::class.java)
                startActivityForResult(i, REQUEST_CODE)
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
        val dialog = builder.create()
        dialog.show()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                if (data.hasExtra("username")) username = data.extras?.getString("username")
            }
        }
        if(username == null){
            val i = Intent(this, FirstTimeActivity::class.java)
            startActivityForResult(i, REQUEST_CODE)
        }
        val filename =  "$filesDir/username.txt"
        val file = File(filename)
        file.bufferedWriter().use { writer ->
            writer.write("$username\n")
            writer.write("never\n")
            writer.write("?\n")
            writer.write("?\n")
        }
        findViewById<TextView>(R.id.usernameView).text = getString(R.string.username)+" $username"
        findViewById<TextView>(R.id.lastSyncView).text = getString(R.string.last_sync)+" "+getString(R.string.never)
        findViewById<TextView>(R.id.numGamesView).text = getString(R.string.number_games)+" ?"
        findViewById<TextView>(R.id.numExpansionsView).text = getString(R.string.number_expansions)+" ?"
        sync(false)
    }
    fun syncClick(v: View) {
        val lastSync = findViewById<TextView>(R.id.lastSyncView).text
        if(lastSync == getString(R.string.sync_in_progress))return
        if(lastSync == getString(R.string.last_sync)+" "+getString(R.string.never)){
            val deleteFlag = findViewById<CheckBox>(R.id.switch1).isChecked
            sync(deleteFlag)
            return
        }
        val lastSyncDate = dateFormat.parse(lastSync.toString().substring(getString(R.string.last_sync).length+1))
        if(lastSyncDate == null){
            val deleteFlag = findViewById<CheckBox>(R.id.switch1).isChecked
            sync(deleteFlag)
            return
        }
        val now = Calendar.getInstance().time
        if(now.time - lastSyncDate.time > 86400000){    //86400000 = 24h
            val deleteFlag = findViewById<CheckBox>(R.id.switch1).isChecked
            sync(deleteFlag)
            return
        }
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.confirm_sync)+" $lastSync")
            .setPositiveButton(getString(R.string.yes)) { dialog, id ->
                val deleteFlag = findViewById<CheckBox>(R.id.switch1).isChecked
                sync(deleteFlag)
            }
            .setNegativeButton(getString(R.string.no)) { dialog, id ->
                dialog.dismiss()
            }
        val dialog = builder.create()
        dialog.show()
    }
    private fun sync(deleteFlag: Boolean) {
        tempLastSync = findViewById<TextView>(R.id.lastSyncView).text.toString()
        findViewById<TextView>(R.id.lastSyncView).text = getString(R.string.sync_in_progress)
        downloadXML("https://boardgamegeek.com/xmlapi2/collection?username=$username&stats=1&own=1&excludesubtype=boardgameexpansion", "$filesDir/games.xml", deleteFlag)
        downloadXML("https://boardgamegeek.com/xmlapi2/collection?username=$username&stats=1&own=1&subtype=boardgameexpansion", "$filesDir/expansions.xml", deleteFlag)
    }
    private fun downloadXML(urlString: String, filename: String, deleteFlag: Boolean){
        CoroutineScope(Dispatchers.IO).launch{
            try{
                val url = URL(urlString)
                val reader = url.openStream().bufferedReader()
                val downloadFile = File(filename).also { it.createNewFile() }
                val writer = FileWriter(downloadFile).buffered()
                var line: String
                while(reader.readLine().also { line = it?.toString() ?: "" } != null)
                    writer.write(line)
                reader.close()
                writer.close()

                withContext(Dispatchers.Main){
                    loadData(urlString, filename, deleteFlag)
                }
            }catch(e: Exception){
                withContext(Dispatchers.Main){

                    findViewById<TextView>(R.id.lastSyncView).text = tempLastSync
                    when (e) {
                        is MalformedURLException ->
                            print("Malformed URL")
                        else ->
                            print("Error")
                    }

                    val incompleteFile = File(filename)
                    if(incompleteFile.exists()) incompleteFile.delete()
                }
            }
        }
    }
    private fun downloadFile(urlString: String, filename: String){
        CoroutineScope(Dispatchers.IO).launch{
            try{
                val url = URL(urlString)
                val file = File(filename)
                if(!file.exists()) {
                    file.createNewFile()
                    url.openStream().use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }
                    withContext(Dispatchers.Main) {
                    }
                }
            }catch(e: Exception){
                withContext(Dispatchers.Main){
                    when (e) {
                        is MalformedURLException ->
                            print("Malformed URL")
                        else ->
                            print("Error")
                    }
                    val incompleteFile = File(filename)
                    if(incompleteFile.exists()) incompleteFile.delete()
                }
            }
        }
    }
    private fun loadData(urlString: String, filename: String, deleteFlag: Boolean){
        if(filename=="$filesDir/games.xml" || filename=="$filesDir/expansions.xml") {
            val file = File(filename)
            if(file.exists()){
                val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
                xmlDoc.documentElement.normalize()
                val err = xmlDoc.getElementsByTagName("errors")
                if(err.length != 0){
                    Toast.makeText(this, getString(R.string.invalid_username), Toast.LENGTH_LONG).show()
                    updateStats()
                    return
                }
                val er = xmlDoc.getElementsByTagName("error")
                if(er.length != 0){
                    Toast.makeText(this, getString(R.string.rate_limit_exceeded), Toast.LENGTH_LONG).show()
                    updateStats()
                    return
                }
                val message = xmlDoc.getElementsByTagName("message")

                if(message.length != 0 && message.item(0).toString().contains("Please try again later for access.")) {
                    Toast.makeText(this, "Waiting for data...", Toast.LENGTH_SHORT).show()
                    File(filename).delete()
                    Thread.sleep(Toast.LENGTH_SHORT.toLong())
                    downloadXML(urlString, filename, deleteFlag)
                    return
                }
                val dbHandler = MyDBHandler(this, null, null, 1)
                val idList: MutableList<Int> = ArrayList()
                val first = xmlDoc.getElementsByTagName("items")
                for (i in 0..first.length-1){
                    val itemNode: Node = first.item(i)
                    if(itemNode.nodeType == Node.ELEMENT_NODE){
                        val temp = itemNode as Element
                        val items = temp.childNodes
                        for( j in 0..items.length-1) {
                            val itemNode: Node = items.item(j)
                            if (itemNode.nodeType == Node.ELEMENT_NODE) {
                                val item = itemNode as Element
                                var id: Int = -1
                                var title: String? = null
                                var pos: Int? = null
                                var year: Int? = null
                                var isBoardGame: Boolean = false
                                var isExpansion: Boolean = false

                                val attr = itemNode.attributes

                                for (i in 0..attr.length - 1) {
                                    val value = attr.item(i)
                                    when (value.nodeName) {
                                        "objectid" -> {
                                            id = value.nodeValue.toInt()
                                        }

                                        "subtype" -> {
                                            if (value.nodeValue == "boardgameexpansion") isExpansion = true
                                            else if (value.nodeValue == "boardgame") isBoardGame = true
                                        }
                                    }
                                }

                                if ((!isBoardGame && !isExpansion) || id == -1) continue
                                val children = item.childNodes
                                for (i in 0..children.length - 1) {
                                    val node = children.item(i)
                                    if(node is Element){
                                        when (node.nodeName) {
                                            "name" -> {
                                                title = node.textContent
                                            }
                                            "thumbnail" -> {
                                                downloadFile(node.textContent.toString(), "$filesDir/thumbnails/$id.jpg")
                                            }
                                            "image" -> {
                                                File("$filesDir/$id/").mkdir()
                                                downloadFile(node.textContent.toString(), "$filesDir/$id/0.jpg")
                                            }
                                            "yearpublished" -> {
                                                year = node.textContent.toInt()
                                            }
                                            "stats" -> {
                                                val stats = node.childNodes
                                                for(i in 0..stats.length - 1){
                                                    val node = stats.item(i)
                                                    if(node is Element){
                                                        if(node.nodeName == "rating"){
                                                            val next = node.childNodes
                                                            for(i in 0..next.length - 1) {
                                                                val node = next.item(i)
                                                                if (node is Element) {
                                                                    if (node.nodeName == "ranks") {
                                                                        val ranks = node.childNodes
                                                                        for (i in 0..ranks.length - 1){
                                                                            val node = ranks.item(i)
                                                                            if(node is Element) {
                                                                                if(node.nodeName == "rank") {
                                                                                    var rank_id: String = ""
                                                                                    var str_pos: String = ""
                                                                                    val attr = node.attributes
                                                                                    for(i in 0..attr.length - 1){
                                                                                        val value = attr.item(i)
                                                                                        when (value.nodeName){
                                                                                            "id" -> {
                                                                                                rank_id = value.nodeValue
                                                                                                pos = 1
                                                                                                    }
                                                                                            "value" -> {
                                                                                                str_pos = value.nodeValue
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                    if(rank_id == "1") {
                                                                                        pos =
                                                                                            if(str_pos != "Not Ranked") str_pos.toInt()
                                                                                            else null
                                                                                        break
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (isExpansion) dbHandler.addExpansion(Expansion(id, title, year, pos))
                                else if (isBoardGame) dbHandler.addGame(Game(id, title, year, pos))
                                idList.add(id)
                            }
                        }
                    }
                }
                if(deleteFlag){
                    val dirs: List<Int> = if (filename == "$filesDir/games.xml") {
                        dbHandler.deleteGames(idList.toList())
                    } else {
                        dbHandler.deleteExpansions(idList.toList())
                    }
                    for (dir in dirs) {
                        File("$dir/").deleteRecursively()
                    }
                }
                updateStats()
                File(filename).delete()
            }
        }
    }
    private fun updateStats(){
        val dbHandler = MyDBHandler(this, null, null, 1)
        val numGames: Int = dbHandler.countGames()
        val numExpansions: Int = dbHandler.countExpansions()
        val lastSync = dateFormat.format(Calendar.getInstance().time)
        val filename =  "$filesDir/username.txt"
        val file = File(filename)
        file.bufferedWriter().use { writer ->
            writer.write("$username\n")
            writer.write("$lastSync\n")
            writer.write("$numGames\n")
            writer.write("$numExpansions\n")
        }
        findViewById<TextView>(R.id.usernameView).text = getString(R.string.username)+" $username"
        findViewById<TextView>(R.id.lastSyncView).text = getString(R.string.last_sync)+" $lastSync"
        findViewById<TextView>(R.id.numGamesView).text = getString(R.string.number_games)+" $numGames"
        findViewById<TextView>(R.id.numExpansionsView).text = getString(R.string.number_expansions)+" $numExpansions"
    }
}