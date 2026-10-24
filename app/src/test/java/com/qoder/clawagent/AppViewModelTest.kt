package com.qoder.clawagent

import com.qoder.clawagent.model.AgentKind
import com.qoder.clawagent.model.Plan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AppViewModelTest {

    @Test
    fun `a project owns one hidden agent reused by its chat`() {
        val vm = AppViewModel()
        val project = vm.createProject("新项目", isWebsite = false)

        assertTrue(vm.projects.any { it.id == project.id })
        val agent = vm.ensureProjectAgent(project)
        assertFalse(agent.visibleInList)
        assertEquals("新项目", agent.name)
        assertSame(agent, vm.ensureProjectAgent(project))
        assertTrue(vm.agents.none { it.visibleInList && it.id == agent.id })
    }

    @Test
    fun `renaming and deleting a project keeps its hidden agent in sync`() {
        val vm = AppViewModel()
        val project = vm.createProject("旧项目", isWebsite = true)
        val agentId = vm.ensureProjectAgent(project).id

        vm.renameProject(project.id, "新项目")
        assertEquals("新项目", vm.projectById(project.id)?.name)
        assertEquals("新项目", vm.agentById(agentId)?.name)

        vm.deleteProject(project.id)
        assertNull(vm.projectById(project.id))
        assertNull(vm.agentById(agentId))
    }

    @Test
    fun `local and third party agents keep their runtime configuration`() {
        val vm = AppViewModel()
        val local = vm.createLocalAgent(
            name = "本地 Llama",
            endpoint = "http://10.0.2.2:11434/v1",
            model = "llama3.2",
            apiKey = "",
            workdir = "local",
        )
        assertEquals(AgentKind.LOCAL, local.kind)
        assertEquals("llama3.2", local.localModel)
        assertTrue(vm.canDeleteAgent(local))

        val preset = com.qoder.clawagent.data.SampleData.thirdPartyAgents.first()
        val thirdParty = vm.createThirdPartyAgent(preset, workdir = "cloud")
        assertEquals(AgentKind.THIRD_PARTY, thirdParty.kind)
        assertEquals(preset.command, thirdParty.command)
    }

    @Test
    fun `saveAgent cannot change the runtime kind or hide a visible agent`() {
        val vm = AppViewModel()
        val local = vm.createLocalAgent(
            name = "本地",
            endpoint = "http://127.0.0.1:11434/v1",
            model = "llama3.2",
            apiKey = "",
            workdir = "local",
        )

        vm.saveAgent(
            local.copy(
                name = "重命名本地",
                kind = AgentKind.COZE,
                visibleInList = false,
                localModel = "qwen2.5",
            ),
        )

        val saved = vm.agentById(local.id)
        assertNotNull(saved)
        assertEquals(AgentKind.LOCAL, saved?.kind)
        assertEquals("qwen2.5", saved?.localModel)
        assertTrue(saved?.visibleInList == true)
    }

    @Test
    fun `seeded agents are protected from deletion`() {
        val vm = AppViewModel()
        val seeded = vm.agentById("heizhua")
        assertNotNull(seeded)
        assertFalse(vm.canDeleteAgent(seeded!!))

        val custom = vm.createAgent("小助手", "workspace", "", emptyList())
        assertTrue(vm.canDeleteAgent(custom))
        vm.deleteAgent(custom.id)
        assertNull(vm.agentById(custom.id))
    }

    @Test
    fun `upgrade toggles a local demo plan without payment state`() {
        val vm = AppViewModel()
        assertEquals(Plan.FREE, vm.plan)
        vm.upgradeToPro()
        assertEquals(Plan.PRO, vm.plan)
    }
}
