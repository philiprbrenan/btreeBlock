from graphviz import Digraph

# Create a Digraph object for the flowchart
dot = Digraph(comment='Flowchart Example')

dot.node('B',    'The Btree algorithm in normal Java')                          # Nodes
dot.node('BS',   'BtreeStuck\nfixed key/value stack')
dot.node('BSS',  'BtreeStuckStatic\nreduced use of new')
dot.node('BSML', 'BtreeSML\nstatic bit memory')
dot.node('BSP',  'BtreeSP\ntransactional')
dot.node('BSA',  'BtreeSA\ntransaction in bit memory');
dot.node('BPA',  'BtreePA\npseudo assembler');
dot.node('BVL',  'Generate the Btree Algorithm in synthesizable Verilog');

dot.node('S',    'Stuck\nfixed key/value stack in normal Java')
dot.node('SS',   'StuckStatic\nreduced use of new')
dot.node('SSML', 'StuckSML\nstatic bit memory')
dot.node('SSP',  'StuckSP\ntransactional')
dot.node('SSA',  'StuckSA\ntransaction in bit memory')
dot.node('SPA',  'StuckPA\npseudo assembler')

dot.node('MPA',  'MemoryLayoutPA\npseudo assembler')

dot.edge('B',    'BS')                                                          # Edges between nodes
dot.edge('BS',   'BSS')
dot.edge('BSS',  'BSML')
dot.edge('BSML', 'BSP')
dot.edge('BSP',  'BSA')
dot.edge('BSA',  'BPA')
dot.edge('BPA',  'BVL')

dot.edge('S',    'SS')
dot.edge('SS',   'SSML')
dot.edge('SSML', 'SSP')
dot.edge('SSP',  'SSA')
dot.edge('SSA',  'SPA')

dot.edge('S',    'BS')
dot.edge('SS',   'BSS')
dot.edge('SSML', 'BSML')
dot.edge('SSP',  'BSP')
dot.edge('SSA',  'BSA')
dot.edge('SPA',  'BPA')


dot.edge('MPA',  'SPA')
dot.edge('MPA',  'BPA')

dot.render('DevelopmentFlowChart', format='png', cleanup=True)                  # Display
dot.view('flowchart_output')
