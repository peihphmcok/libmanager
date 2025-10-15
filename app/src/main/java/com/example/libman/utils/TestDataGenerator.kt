package com.example.libman.utils

import com.example.libman.models.Author
import com.example.libman.models.Book
import com.example.libman.models.Loan
import com.example.libman.models.User
import java.util.*

object TestDataGenerator {
    
    fun generateSampleBooks(): List<Book> {
        return listOf(
            Book(
                title = "Truyện Kiều",
                author = "Nguyễn Du",
                category = "Văn học cổ điển",
                publishedYear = 1820
            ),
            Book(
                title = "Chí Phèo",
                author = "Nam Cao",
                category = "Văn học hiện thực",
                publishedYear = 1941
            ),
            Book(
                title = "Dế Mèn phiêu lưu ký",
                author = "Tô Hoài",
                category = "Văn học thiếu nhi",
                publishedYear = 1941
            ),
            Book(
                title = "Romeo và Juliet",
                author = "William Shakespeare",
                category = "Kịch",
                publishedYear = 1597
            ),
            Book(
                title = "Chiến tranh và Hòa bình",
                author = "Leo Tolstoy",
                category = "Tiểu thuyết sử thi",
                publishedYear = 1869
            ),
            Book(
                title = "Những người khốn khổ",
                author = "Victor Hugo",
                category = "Tiểu thuyết xã hội",
                publishedYear = 1862
            ),
            Book(
                title = "Số đỏ",
                author = "Vũ Trọng Phụng",
                category = "Tiểu thuyết châm biếm",
                publishedYear = 1936
            ),
            Book(
                title = "Tắt đèn",
                author = "Ngô Tất Tố",
                category = "Tiểu thuyết hiện thực",
                publishedYear = 1939
            )
        )
    }
    
    fun generateSampleAuthors(): List<Author> {
        return listOf(
            Author(
                name = "Nguyễn Du",
                bio = "Đại thi hào dân tộc Việt Nam, tác giả của Truyện Kiều",
                nationality = "Việt Nam",
                birthYear = 1765
            ),
            Author(
                name = "Nam Cao",
                bio = "Nhà văn hiện thực xuất sắc của văn học Việt Nam",
                nationality = "Việt Nam", 
                birthYear = 1915
            ),
            Author(
                name = "Tô Hoài",
                bio = "Nhà văn nổi tiếng với tác phẩm Dế Mèn phiêu lưu ký",
                nationality = "Việt Nam",
                birthYear = 1920
            ),
            Author(
                name = "William Shakespeare",
                bio = "Nhà thơ và nhà viết kịch vĩ đại nhất của nước Anh",
                nationality = "Anh",
                birthYear = 1564
            ),
            Author(
                name = "Leo Tolstoy",
                bio = "Tiểu thuyết gia Nga nổi tiếng với Chiến tranh và Hòa bình",
                nationality = "Nga",
                birthYear = 1828
            ),
            Author(
                name = "Victor Hugo",
                bio = "Nhà văn Pháp nổi tiếng với Những người khốn khổ",
                nationality = "Pháp",
                birthYear = 1802
            ),
            Author(
                name = "Vũ Trọng Phụng",
                bio = "Nhà văn Việt Nam nổi tiếng với Số đỏ",
                nationality = "Việt Nam",
                birthYear = 1912
            ),
            Author(
                name = "Ngô Tất Tố",
                bio = "Nhà văn Việt Nam nổi tiếng với Tắt đèn",
                nationality = "Việt Nam",
                birthYear = 1894
            )
        )
    }
    
    fun generateSampleUsers(): List<User> {
        return listOf(
            User(
                username = "admin",
                name = "Quản trị viên",
                email = "admin@library.com",
                role = "admin"
            ),
            User(
                username = "user1",
                name = "Nguyễn Văn A",
                email = "user1@library.com",
                role = "user"
            ),
            User(
                username = "user2",
                name = "Trần Thị B",
                email = "user2@library.com",
                role = "user"
            ),
            User(
                username = "user3",
                name = "Lê Văn C",
                email = "user3@library.com",
                role = "user"
            )
        )
    }
    
    fun generateSampleLoans(books: List<Book>, users: List<User>): List<Loan> {
        val now = Date()
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        
        return listOf(
            Loan(
                book = books[0],
                user = users[1],
                borrowDate = dateFormat.format(now),
                dueDate = dateFormat.format(Date(now.time + 14 * 24 * 60 * 60 * 1000)),
                status = "borrowed"
            ),
            Loan(
                book = books[1],
                user = users[2],
                borrowDate = dateFormat.format(now),
                dueDate = dateFormat.format(Date(now.time + 14 * 24 * 60 * 60 * 1000)),
                status = "borrowed"
            ),
            Loan(
                book = books[2],
                user = users[3],
                borrowDate = dateFormat.format(Date(now.time - 7 * 24 * 60 * 60 * 1000)),
                dueDate = dateFormat.format(Date(now.time - 3 * 24 * 60 * 60 * 1000)),
                status = "overdue"
            )
        )
    }
}
