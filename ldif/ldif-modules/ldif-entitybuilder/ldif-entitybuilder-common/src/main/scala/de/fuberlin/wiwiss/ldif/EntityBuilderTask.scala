package de.fuberlin.wiwiss.ldif

import ldif.util.Identifier
import ldif.module.ModuleTask
import ldif.entity.EntityDescription

class EntityBuilderTask (override val name : Identifier, val entityDescriptions : IndexedSeq[EntityDescription]) extends ModuleTask