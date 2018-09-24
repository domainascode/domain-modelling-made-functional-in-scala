package io.github.ilyazinovich.dmmf

import cats.Apply
import cats.data.Validated._
import cats.data._
import cats.implicits._
import io.github.ilyazinovich.dmmf.ProductCode.CheckProductCodeExist

trait PlaceOrder {

  type CheckAddressExist = UnvalidatedAddress => Either[Error, Address]

  def validateOrder(checkProductCodeExists: CheckProductCodeExist,
                    checkAddressExist: CheckAddressExist,
                    unvalidatedOrder: UnvalidatedOrder): Unit = {
    //ValidatedNel[Error, Order] = {
  }

  def validateOrderId(orderId: String): Validated[Error, OrderId] = {
    OrderId.create(orderId).toValidated
  }

  def validateAddress(address: UnvalidatedAddress, checkAddressExist: CheckAddressExist): Validated[Error, Address] = {
    checkAddressExist(address).toValidated
  }

  def validateOrderLines(orderLines: List[UnvalidatedOrderLine],
                         checkProductCodeExist: CheckProductCodeExist): ValidatedNel[Error, List[OrderLine]] = {
    orderLines.map { orderLine =>
      val validatedOrderLineId = OrderLineId.create(orderLine.orderLineId).toValidatedNel
      val validatedProductCode = ProductCode.create(orderLine.productCode, checkProductCodeExist).toValidatedNel
      val validatedProductQuantity = validatedProductCode.andThen { productCode =>
        ProductQuantity.create(productCode, orderLine.quantity).toValidatedNel
      }
      Apply[ValidatedNel[Error, ?]].map3(validatedOrderLineId, validatedProductCode, validatedProductQuantity) {
        case (orderLineId, productCode, productQuantity) => OrderLine(orderLineId, productCode, productQuantity)
      }
    }
  }
}

case class UnvalidatedOrder(orderId: String, address: UnvalidatedAddress, orderLines: List[UnvalidatedOrderLine])

case class UnvalidatedAddress(addressLine: String)

case class UnvalidatedOrderLine(orderLineId: String, productCode: String, quantity: Double)

case class Order(orderId: OrderId, address: Address, orderLines: List[OrderLine])

case class Address(addressLine: String)

case class OrderLine(orderLineId: OrderLineId, productCode: ProductCode, quantity: ProductQuantity)
