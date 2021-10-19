package com.nice.sqlite.core.ddl

import com.nice.sqlite.core.Dialect
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.TableSubject

class TableCreateStatement<T : Table>(
    val subject: TableSubject<T>,
    val definitions: Sequence<Definition>
) : Statement {

    override fun toString(dialect: Dialect): String = dialect.build(this)

}

class TableAlterStatement<T : Table>(
    val subject: TableSubject<T>,
    val definitions: Sequence<Definition>
) : Statement {

    override fun toString(dialect: Dialect): String = dialect.build(this)

}

class TableDropStatement<T : Table>(
    val subject: TableSubject<T>,
    val definitions: Sequence<Definition>
) : Statement {

    override fun toString(dialect: Dialect): String = dialect.build(this)

}