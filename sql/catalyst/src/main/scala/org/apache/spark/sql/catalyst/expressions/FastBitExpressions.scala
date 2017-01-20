
package org.apache.spark.sql.catalyst.expressions

import org.apache.spark.sql.catalyst.expressions.codegen.CodegenFallback
import org.apache.spark.sql.fastbit.FastBitClient

import org.apache.spark.sql.types.{DataType, IntegerType}


/**
  * @author sasini.
  */


@ExpressionDescription(
  usage = "_FUNC_(select,dataDir,where) - Count operation",
  extended = "> SELECT _FUNC_(count(*),truck_index,miles>15000);")
abstract class FastBitCount(select: Expression, dataDir: Expression, where: Expression)
  extends TernaryExpression with  CodegenFallback{

  override def dataType: DataType = IntegerType
  override def nullSafeEval(selectClause: Any, dataDir: Any, whereClause: Any): Any = {

    val fb: FastBitClient = new FastBitClient()

    val count = fb.countQuery(selectClause.asInstanceOf[String],
      dataDir.asInstanceOf[String],
      whereClause.asInstanceOf[String])

    count
  }

}
