package com.me.dio.credit.application.system.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.me.dio.credit.application.system.dto.request.CreditDto
import com.me.dio.credit.application.system.dto.request.CustomerDto
import com.me.dio.credit.application.system.repository.CreditRepository
import com.me.dio.credit.application.system.repository.CustomerRepository
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ContextConfiguration
class CreditResourceTest {
    @Autowired
    private lateinit var creditRepository: CreditRepository
    @Autowired
    private lateinit var customerRepository: CustomerRepository
    @Autowired
    private lateinit var mockMvc: MockMvc
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    companion object {
        const val URL: String = "/api/credits"
    }

    @BeforeEach
    fun setup() {
        customerRepository.deleteAll()
        creditRepository.deleteAll()
    }

    @AfterEach
    fun tearDown() {
        customerRepository.deleteAll()
        creditRepository.deleteAll()
    }

    @Test
    fun `should create a credit and return 201 status`() {
        //given
        customerRepository.save(builderCustomerDto().toEntity())
        val creditDto: CreditDto = builderCreditDto()
        val valueAsString: String = objectMapper.writeValueAsString(creditDto)
        //when
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not create a credit with numberOfInstallments greater than 48 and return 400 status`() {
        //given
        customerRepository.save(builderCustomerDto().toEntity())
        val customerDto: CreditDto = builderCreditDto(numberOfInstallments = 49)
        val valueAsString: String = objectMapper.writeValueAsString(customerDto)
        //when
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .content(valueAsString)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not create a credit with dayFirstInstallment is before 3 months and return 400 status`() {
        //given
        customerRepository.save(builderCustomerDto().toEntity())
        val customerDto: CreditDto = builderCreditDto(dayFirstOfInstallment = LocalDate.now().plusMonths(3))
        val valueAsString: String = objectMapper.writeValueAsString(customerDto)
        //when
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .content(valueAsString)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should find credit by customer id and return 200 status`() {
        //given
        val customerId: Long = customerRepository.save(builderCustomerDto().toEntity()).id!!
        creditRepository.save(builderCreditDto().toEntity())
        //when
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.get(URL)
                .param("customerId", customerId.toString())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should find credit by customer id with empty array and return 200 status`() {
        //given
        customerRepository.save(builderCustomerDto().toEntity())
        creditRepository.save(builderCreditDto().toEntity())
        val customerId: Long = 2L
        //when
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.get(URL)
                .param("customerId", customerId.toString())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should find credit by customer id and creditCode with empty array and return 200 status`() {
        //given
        val customerId: Long = customerRepository.save(builderCustomerDto().toEntity()).id!!
        val creditCode = creditRepository.save(builderCreditDto().toEntity()).creditCode!!
        //when
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.get("$URL/$creditCode?customerId=$customerId")
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not find credit by customer id and creditCode with empty array and return 200 status`() {
        //given
        val customerId: Long = customerRepository.save(builderCustomerDto().toEntity()).id!!
        creditRepository.save(builderCreditDto().toEntity()).creditCode!!
        val creditCode: UUID = UUID.randomUUID()
        //when
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.get("$URL/$creditCode?customerId=$customerId")
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.exception").value("class com.me.dio.credit.application.system.exception.BusinessException"))
            .andDo(MockMvcResultHandlers.print())
    }

    private fun builderCreditDto(
        creditValue: BigDecimal = BigDecimal.valueOf(100),
        dayFirstOfInstallment: LocalDate = LocalDate.now().plusMonths(2L),
        numberOfInstallments: Int = 15,
        customerId: Long = 1L
    ) = CreditDto(
        creditValue = creditValue,
        dayFirstOfInstallment = dayFirstOfInstallment,
        numberOfInstallments = numberOfInstallments,
        customerId = customerId
    )

    private fun builderCustomerDto(
        firstName: String = "Cami",
        lastName: String = "Cavalcante",
        cpf: String = "28475934625",
        email: String = "camila@email.com",
        income: BigDecimal = BigDecimal.valueOf(1000.0),
        password: String = "1234",
        zipCode: String = "000000",
        street: String = "Rua da Cami, 123",
    ) = CustomerDto(
        firstName = firstName,
        lastName = lastName,
        cpf = cpf,
        email = email,
        income = income,
        password = password,
        zipCode = zipCode,
        street = street
    )
}