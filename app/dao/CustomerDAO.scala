package dao


import com.google.inject.Inject
import models.Customer
import org.joda.time.DateTime
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import dao.CustomColumnTypes._
import scala.concurrent.{ExecutionContext, Future}

trait CustomerComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  final class CustomerTable(tag: Tag) extends Table[Customer](tag, "Customer") {

    def customerno = column[Int]("CUSTOMERNO", O.PrimaryKey, O.AutoInc)

    def customername = column[String]("CUSTOMERNAME")

    def customertel = column[Option[String]]("CUSTOMERTEL")

    def creator = column[Option[Int]]("CREATOR")

    def createtime = column[Option[DateTime]]("CREATETIME")

    def updater = column[Option[Int]]("UPDATER")

    def updatetime = column[Option[DateTime]]("UPDATETIME")

    def * = (customerno, customername, customertel, creator, createtime, updater, updatetime).shaped <>
      ( {
        case (customerno, customername, customertel, creator, createtime, updater, updatetime) =>
          Customer(customerno, customername, customertel, creator, createtime, updater, updatetime)
      }, { c: Customer =>
        Some((c.CustomerNo, c.CustomerName, c.CustomerTel, c.Creator, c.CreateTime, c.Updater, c.UpdateTime))
      })
  }

}

class CustomerDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,
                            implicit val ec: ExecutionContext) extends CustomerComponent
  with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private lazy val customers = TableQuery[CustomerTable]

  def createTable(): Unit = {
    db.run(
      customers.schema.create
    )
  }

  def add(c: Customer): Future[Int] = {
    db.run(
      customers += c
    )
  }

  def addInBatch(cs: Seq[Customer]): Future[Int] = {
    db.run(
      customers ++= cs
    ).map(_ => cs.length)
  }

  def update(c: Customer): Future[Int] = {
    db.run(
      customers.filter(_.customerno === c.CustomerNo).update(c)
    )
  }

  def delete(cNo: Int): Future[Int] = {
    db.run(
      customers.filter(_.customerno === cNo).delete
    )
  }

  def get(cNo: Int): Future[Option[Customer]] = {
    db.run(
      customers.filter(_.customerno === cNo).result
    ) map (_.headOption)
  }

}