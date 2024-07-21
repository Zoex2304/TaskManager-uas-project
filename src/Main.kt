import java.io.File

data class Task(var id:Int, var description:String, var status :Boolean = false)

class TaskManager(private val username : String,private val password : Int){
    private var tasks : MutableList<Task> = mutableListOf()
    private var nextId = 1
    private var fileName = "${username}.txt"

    init {
        loadTask()
    }

    private fun saveTask(){
        File(fileName).printWriter().use { out ->
            tasks.forEach { item ->
                out.println("${item.id}|${item.description}|${item.status}")
            }
        }
    }

    private fun loadTask(){
        val userTaskFile = File(fileName)
        if(userTaskFile.exists()){
            userTaskFile.readLines().forEach { line ->
                val parts = line.split("|")
                if(parts.size == 3){
                    val id = parts[0].toInt()
                    val description = parts[1]
                    val status = parts[2].toBoolean()
                    if(id >= 0) id + 1
                    tasks.add(Task(id,description,status))
                }
            }
        }
    }

    fun saveAccount(){
        val accountFile = "${username}-account.txt"
        File(accountFile).appendText("${username}|${password}")
    }

    companion object{
        fun loadAccount(username: String,password: Int) : Boolean{
            val accountFile = File("${username}-account.txt")
            if(accountFile.exists()){
                accountFile.readLines().forEach { line ->
                    val parts = line.split("|")
                    if (parts.size == 2 && parts[0] == username && parts[1].toInt() == password) return true
                }
            }
            return false
        }
    }

    fun addTask(description: String){
        val task = Task(nextId++,description)
        tasks.add(task)
        saveTask()
    }
    fun viewTask() : List<Task>{
        return  tasks.toList()
    }
    fun deleteTask(id: Int) : Boolean{
        val removed = tasks.removeIf{it.id == id}
        if (removed) {
            saveTask()
        }
        return removed
    }
    fun markTaskCompleted(id: Int) : Boolean{
        val task = tasks.find { it.id == id }
        return  if (task != null){
            task.status = true
            true
        }else{
            false
        }
    }
}

interface TaskAction{
    fun execute (taskManager: TaskManager)
}

class AddTask(private var description: String) : TaskAction{
    override fun execute(taskManager: TaskManager) {
        if(description.isNotBlank()){
            taskManager.addTask(description)
            println("task added successfully")
        }else{
            println("task must be filled")
        }
    }
}

class ViewTask : TaskAction{
    override fun execute(taskManager: TaskManager) {
        val tasks = taskManager.viewTask()
        if (tasks.isNotEmpty()){

            tasks.forEachIndexed {index, item ->
                item.id = index + 1
                println("${item.id},${item.description},${item.status},")
            }
            println("Enter a task id to mark as completed if there is nothing completed insert 0: ")
            val id = readlnOrNull()?.toIntOrNull() ?: 0
            if(taskManager.markTaskCompleted(id)){
                println("Task marked as completed")
            }
        }else{
            println("you don't have any note yet")
        }
    }
}

class DeleteTask(private var id: Int) : TaskAction{
    override fun execute(taskManager: TaskManager) {
        if(taskManager.deleteTask(id)){
            println("Deleted successfully")
        }else{
            println("task id not found in note")
        }
    }
}

class SaveAccount : TaskAction{
    override fun execute(taskManager: TaskManager) {
        taskManager.saveAccount()
    }
}

class RedirectToMainMenu : TaskAction{
    override fun execute(taskManager: TaskManager) {
        while (true){
            val menu : MutableList<String> = mutableListOf("[1].View Task\n","[2].Add Task\n","[3].Delete Task\n","[4].Exit\n","Which One Should I Execute ? : ")
            menu.forEach { item -> print(item) }

            val action : TaskAction? =  when(val choose = readlnOrNull()?.toIntOrNull() ?: 0){
                1 -> ViewTask()
                2 -> {
                    println("let me know what thing did u plan for right now (insert here) : ")
                    val description = readlnOrNull()?.takeIf { it.isNotBlank() } ?: "nothing"
                    if (description.isNotEmpty()){
                        AddTask(description)
                    }else{
                        null
                    }
                }
                3 -> {
                    taskManager.viewTask().forEachIndexed{index, item ->
                        println("${item.id},${item.description},${item.status},")
                    }
                    print("w1hich one should i delete ? : ")
                    val id = readlnOrNull()?.toIntOrNull() ?: 0
                    if(taskManager.deleteTask(id)){
                        DeleteTask(id)
                    }else{
                        null
                    }
                }
                4 -> {
                    println("have a nice day,thank u...")
                    break
                }
                else -> {
                    println("sorry there is no $choose in Available Menu")
                    null
                }
            }
            action?.execute(taskManager)
        }
    }
}

fun main() {
    while (true){
        print("[1].i am new here\n[2].already have an account\nplease input which one do u want : ")
        when (val choose = readlnOrNull()?.toIntOrNull() ?: 0){
            1 -> {
                while (true){
                    try {
                        fun String.isNotLetter() : Boolean{
                            return !this.matches(Regex("^[a-zA-Z]+$"))
                        }
                        print("\n\nyour name please : ")
                        val username = readlnOrNull()?.takeIf { it.isNotEmpty() } ?: "0"
                        if (username.isNotLetter()){
                            throw IllegalArgumentException("username should be inserted and make sure its not number")
                        }
                        print("make password (only several number) : ")
                        val password = readlnOrNull()?.toIntOrNull() ?: throw IllegalArgumentException("this field needs password to continue,please inserted")
                        print("confirm password : ")
                        val confirmPassword = readlnOrNull()?.toIntOrNull() ?: throw IllegalArgumentException("this field needs confirm password to continue,please inserted")

                        if (password != confirmPassword){
                            throw IllegalArgumentException("invalid password")
                        }else{
                            val taskManager = TaskManager(username,password)
                            if(TaskManager.loadAccount(username,password)){
                                println("Your account has been created before")
                                RedirectToMainMenu().execute(taskManager)
                            }
                            println("successfully created an account, What should i do for you $username")
                            SaveAccount().execute(taskManager)
                            RedirectToMainMenu().execute(taskManager)
                        }
                        break
                    }catch (error:IllegalArgumentException){
                        println(error.message)
                    }
                }
                break
            }
            2 -> {
                while (true){
                    try {
                        print("\n\nusername : ")
                        val username = readlnOrNull()?.takeIf { it.isNotEmpty() } ?: throw IllegalArgumentException("username should be inserted")
                        print("password : ")
                        val password = readlnOrNull()?.toIntOrNull() ?: throw IllegalArgumentException("this field needs password to continue,please inserted")

                        if (TaskManager.loadAccount(username,password)){
                            println("login successfully")
                            val taskManager = TaskManager(username,password)
                            RedirectToMainMenu().execute(taskManager)
                            break
                        }else{
                            throw  IllegalArgumentException("invalid username anda password")
                        }
                    }catch (error:IllegalArgumentException){
                        println(error.message)
                    }
                }
                break
            }
            else -> {
                println("There Is no $choose in available options")
            }
        }
    }
}
