import com.example.staffmanagement.ui.viewmodels.LoginViewModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        viewModel = LoginViewModel()
    }

    @Test
    fun `checkEmail should return true for valid email`() {
        val validEmails = listOf(
            "test@example.com",
            "user.name@domain.co",
            "user_name@domain.com",
            "user-name@domain.org",
            "user+name@domain.net"
        )

        validEmails.forEach { email ->
            assertTrue("Expected true for email: $email", viewModel.checkEmail(email))
        }
    }

    @Test
    fun `checkEmail should return false for invalid email`() {
        val invalidEmails = listOf(
            "",
            "plainaddress",
            "user@.com",
            "user@com",
            "user@domain,com",
            "@domain.com",
        )

        invalidEmails.forEach { email ->
            assertFalse("Expected false for email: $email", viewModel.checkEmail(email))
        }
    }

    @Test
    fun `checkPassword should return true for valid password`() {
        val validPasswords = listOf(
            "abcdef",
            "123456",
            "abc123",
            "abcdefghi",
            "Abc123XYZ"
        )

        validPasswords.forEach { password ->
            assertTrue("Expected true for password: $password", viewModel.checkPassword(password))
        }
    }

    @Test
    fun `checkPassword should return false for invalid password`() {
        val invalidPasswords = listOf(
            "",
            "abc",
            "abcdef12345",
            "abc def",
            "123#",
            "!!@@##",
        )

        invalidPasswords.forEach { password ->
            assertFalse("Expected false for password: $password", viewModel.checkPassword(password))
        }
    }
}