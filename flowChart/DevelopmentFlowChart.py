from graphviz import Digraph

# Create a Digraph object for the flowchart
dot = Digraph(comment='Flowchart Example')

dot.node('B',    'Btree\nnormal java')                                          # Nodes
dot.node('BS',   'BtreeStuck\nfixed key/value stack')
dot.node('BSS',  'BtreeStuckStatic\nreduced use of new')
dot.node('BSML', 'BtreeSML\nstatic bit memory')
dot.node('BSP',  'BtreeSP\nparameterized by a single structure')

dot.node('S',    'Stuck\nfixed key/value stack in normal java')
dot.node('SS',   'StuckStatic\nreduced use of new')
dot.node('SSML', 'StuckSML\nstatic bit memory')
dot.node('SSP',  'StuckSP\nsingle parameter')
dot.node('SSA',  'StuckSA\nsingle parameter in bit memory')
dot.node('SPA',  'StuckPA\npsuedo assembler')

dot.node('L',    'Layout\nmemory layout in normal java')
dot.node('M',    'Memory\nbit memory in normal java')

dot.edge('B',    'BS')                                                          # Edges between nodes
dot.edge('BS',   'BSS')
dot.edge('BSS',  'BSML')
dot.edge('BSML', 'BSP')

dot.edge('S',    'SS')
dot.edge('S',    'BS')
dot.edge('SS',   'SSML')
dot.edge('SS',   'BSS')
dot.edge('SSML', 'BSML')
dot.edge('SSML', 'SSP')
dot.edge('SSP',  'SSA')
dot.edge('SSA',  'SPA')
dot.edge('SSP',  'BSP')

dot.edge('L',    'SSML'); dot.edge('M',    'SSML')
dot.edge('L',    'BSML'); dot.edge('M',    'BSML')

dot.edge('L',    'SSP');  dot.edge('M',    'SSP')
dot.edge('L',    'SSA');  dot.edge('M',    'SSA')
dot.edge('L',    'SPA');  dot.edge('M',    'SPA')

dot.render('DevelopmentFlowChart', format='png', cleanup=True)                  # Display
dot.view('flowchart_output')
