/**
 * Creating a sidebar enables you to:
 - create an ordered group of docs
 - render a sidebar for each doc of that group
 - provide next/previous navigation

 The sidebars can be generated from the filesystem, or explicitly defined here.

 Create as many sidebars as you want.
 */

// @ts-check

/** @type {import('@docusaurus/plugin-content-docs').SidebarsConfig} */
const sidebars = {
  // By default, Docusaurus generates a sidebar from the docs folder structure
  // docsSidebar: [{type: 'autogenerated', dirName: '.'}],

  docsSidebar: [
    {
      type: 'category',
      collapsible: false,
      label: 'Getting Started',
      link: {
        type: 'doc',
        id: 'index',
      },
      items: ['gettingstarted/connection', 'gettingstarted/collection', 'gettingstarted/documents'],
    },
    {
      type: 'category',
      collapsible: false,
      label: 'Operations',
      link: {
        type: 'doc',
        id: 'operations',
      },
      items: ['operations/indexes', 'operations/find', 'operations/update', 'operations/distinct', 'operations/aggregate', 'operations/watch'],
    },
    {
      type: 'doc',
      id: 'embedded',
      label: 'Embedded MongoDB',
    },
    {
      type: 'doc',
      id: 'circe',
      label: 'Circe',
    },
    {
      type: 'doc',
      id: 'zio',
      label: 'ZIO',
    }
  ]

  // But you can create a sidebar manually
  /*
  tutorialSidebar: [
    'intro',
    'hello',
    {
      type: 'category',
      label: 'Tutorial',
      items: ['tutorial-basics/create-a-document'],
    },
  ],
   */
};

export default sidebars;
