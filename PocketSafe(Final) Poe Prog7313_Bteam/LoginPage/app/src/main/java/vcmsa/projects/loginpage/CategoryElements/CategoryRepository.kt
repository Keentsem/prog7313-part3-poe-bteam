package vcmsa.projects.loginpage.CategoryElements

class CategoryRepository(private val categoryDao: CategoryDao) {

    suspend fun insertCategory(category: Category) {
        // Check if the category already exists
        val categoryExists = categoryDao.countByName(category.name)
        if (categoryExists == 0) {
            try {
                categoryDao.insertCategory(category)
            } catch (e: Exception) {
                // Handle error
                println("Error inserting category: ${e.message}")
            }
        } else {
            // Category already exists, handle accordingly
            println("Category with name ${category.name} already exists!")
        }
    }
}
