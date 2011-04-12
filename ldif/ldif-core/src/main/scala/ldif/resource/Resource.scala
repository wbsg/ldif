package ldif.resource

class Resource(val uri : String, val factums : IndexedSeq[Traversable[Factum]], resourceFormat : ResourceFormat)
{
}