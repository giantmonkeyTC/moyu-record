package cn.troph.tomon.core.collections

import cn.troph.tomon.core.utils.Collection
import cn.troph.tomon.core.structures.Base

open class BaseCollection<T : Base>(m: Map<String, T>?) : Collection<T>(m) {

}