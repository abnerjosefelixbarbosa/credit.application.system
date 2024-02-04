package com.me.dio.credit.application.system.service.impl

import com.me.dio.credit.application.system.entity.Customer
import com.me.dio.credit.application.system.exception.BusinessException
import com.me.dio.credit.application.system.repository.CustomerRepository
import com.me.dio.credit.application.system.service.ICustomerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class CustomerService: ICustomerService {
    @Autowired private lateinit var customerRepository: CustomerRepository

    override fun save(customer: Customer): Customer = this.customerRepository.save(customer)

    override fun findById(id: Long): Customer = this.customerRepository.findById(id)
        .orElseThrow{throw BusinessException("Id $id not found") }

    override fun delete(id: Long) {
        val customer: Customer = this.findById(id)
        this.customerRepository.delete(customer)
    }
}