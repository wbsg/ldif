package ldif

import ldif.module.ModuleConfig
import ldif.entity.EntityDescription

class EntityBuilderConfig(val entityDescriptions : IndexedSeq[EntityDescription]) extends ModuleConfig