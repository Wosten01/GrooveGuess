import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {

    fun create(user: User): User = userRepository.save(user)

    fun find(id: Long): User = userRepository.findById(id)
        .orElseThrow { RuntimeException("User not found") }

    fun update(user: User): User = userRepository.save(user)

    fun delete(id: Long) = userRepository.deleteById(id)
}
