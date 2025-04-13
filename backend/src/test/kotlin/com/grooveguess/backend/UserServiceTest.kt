import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@AutoConfigureTestDatabase
class UserServiceTest @Autowired constructor(
    val userService: UserService,
    val userRepository: UserRepository
) {

    @BeforeEach
    fun setup() = userRepository.deleteAll()

    @Test
    fun `create user and find it`() {
        val user = userService.create(User(username = "john", email = "john@mail.com", password = "123"))
        val found = userService.find(user.id)
        assertEquals("john", found.username)
    }

    @Test
    fun `update user`() {
        val user = userService.create(User(username = "old", email = "old@mail.com", password = "123"))
        val updated = user.copy(username = "new")
        val saved = userService.update(updated)
        assertEquals("new", saved.username)
    }

    @Test
    fun `delete user`() {
        val user = userService.create(User(username = "del", email = "del@mail.com", password = "123"))
        userService.delete(user.id)
        assertThrows<RuntimeException> { userService.find(user.id) }
    }
}
